package com.bakkenbaeck.token.presenter;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.util.Pair;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.PathInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.bakkenbaeck.token.R;
import com.bakkenbaeck.token.crypto.HDWallet;
import com.bakkenbaeck.token.crypto.util.TypeConverter;
import com.bakkenbaeck.token.model.local.ActivityResultHolder;
import com.bakkenbaeck.token.model.local.Conversation;
import com.bakkenbaeck.token.model.local.PendingTransaction;
import com.bakkenbaeck.token.model.local.SofaMessage;
import com.bakkenbaeck.token.model.local.User;
import com.bakkenbaeck.token.model.network.Balance;
import com.bakkenbaeck.token.model.sofa.Command;
import com.bakkenbaeck.token.model.sofa.Control;
import com.bakkenbaeck.token.model.sofa.Init;
import com.bakkenbaeck.token.model.sofa.InitRequest;
import com.bakkenbaeck.token.model.sofa.Message;
import com.bakkenbaeck.token.model.sofa.Payment;
import com.bakkenbaeck.token.model.sofa.PaymentRequest;
import com.bakkenbaeck.token.model.sofa.SofaAdapters;
import com.bakkenbaeck.token.model.sofa.SofaType;
import com.bakkenbaeck.token.manager.store.ConversationStore;
import com.bakkenbaeck.token.manager.store.PendingTransactionStore;
import com.bakkenbaeck.token.util.LogUtil;
import com.bakkenbaeck.token.util.OnNextSubscriber;
import com.bakkenbaeck.token.util.OnSingleClickListener;
import com.bakkenbaeck.token.util.PaymentType;
import com.bakkenbaeck.token.util.SingleSuccessSubscriber;
import com.bakkenbaeck.token.util.SoundManager;
import com.bakkenbaeck.token.view.Animation.SlideUpAnimator;
import com.bakkenbaeck.token.view.BaseApplication;
import com.bakkenbaeck.token.view.activity.AmountActivity;
import com.bakkenbaeck.token.view.activity.ChatActivity;
import com.bakkenbaeck.token.view.activity.ViewUserActivity;
import com.bakkenbaeck.token.view.adapter.MessageAdapter;
import com.bakkenbaeck.token.view.adapter.listeners.OnItemClickListener;
import com.bakkenbaeck.token.view.custom.ControlRecyclerView;
import com.bakkenbaeck.token.view.custom.ControlView;
import com.bakkenbaeck.token.view.custom.SpeedyLinearLayoutManager;
import com.bakkenbaeck.token.view.notification.ChatNotificationManager;
import com.bumptech.glide.Glide;

import java.io.IOException;
import java.math.BigInteger;

import io.realm.RealmList;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;


public final class ChatPresenter implements
        Presenter<ChatActivity> {

    private static final int REQUEST_RESULT_CODE = 1;
    private static final int PAY_RESULT_CODE = 2;

    private ChatActivity activity;
    private MessageAdapter messageAdapter;
    private ConversationStore conversationStore;
    private User remoteUser;
    private SpeedyLinearLayoutManager layoutManager;
    private SofaAdapters adapters;
    private HDWallet userWallet;
    private Subscription getUserSubscription;
    private Dialog notEnoughFundsDialog;
    private boolean firstViewAttachment = true;
    private int lastVisibleMessagePosition;

    @Override
    public void onViewAttached(final ChatActivity activity) {
        this.activity = activity;

        if (firstViewAttachment) {
            firstViewAttachment = false;
            initLongLivingObjects();
        }
        initShortLivingObjects();
    }

    private void initLongLivingObjects() {
        initMessageAdapter();
        initPendingTransactionStore();

        BaseApplication.get()
                .getTokenManager()
                .getWallet()
                .subscribeOn(Schedulers.io())
                .subscribe(new SingleSuccessSubscriber<HDWallet>() {
                    @Override
                    public void onSuccess(final HDWallet wallet) {
                        userWallet = wallet;
                        this.unsubscribe();
                    }
                });
    }

    private void initMessageAdapter() {
        this.adapters = new SofaAdapters();
        this.messageAdapter = new MessageAdapter()
                .addOnPaymentRequestApproveListener(this.handlePaymentRequestApprove)
                .addOnPaymentRequestRejectListener(this.handlePaymentRequestReject);
    }

    private void initPendingTransactionStore() {
        BaseApplication
                .get()
                .getTokenManager()
                .getTransactionManager()
                .getPendingTransactionObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this.handlePendingTransactionChange);
    }

    private void initShortLivingObjects() {
        initLayoutManager();
        initAdapterAnimation();
        initRecyclerView();
        initButtons();
        initControlView();
        processIntentData();
        initLoadingSpinner(this.remoteUser);
    }

    private void processIntentData() {
        if (this.remoteUser == null) {
            final String remoteUserAddress = this.activity.getIntent().getStringExtra(ChatActivity.EXTRA__REMOTE_USER_ADDRESS);
            fetchUserFromAddress(remoteUserAddress);
            return;
        }

        updateUiFromRemoteUser();
        processPaymentFromIntent();
    }

    private void processPaymentFromIntent() {
        if (this.remoteUser == null) {
            return;
        }

        final String value = this.activity.getIntent().getStringExtra(ChatActivity.EXTRA__ETH_AMOUNT);
        final int paymentAction = this.activity.getIntent().getIntExtra(ChatActivity.EXTRA__PAYMENT_ACTION, 0);

        this.activity.getIntent().removeExtra(ChatActivity.EXTRA__ETH_AMOUNT);
        this.activity.getIntent().removeExtra(ChatActivity.EXTRA__PAYMENT_ACTION);

        if (value == null || paymentAction == 0) {
            return;
        }

        if (paymentAction == PaymentType.TYPE_SEND) {
            sendPaymentWithValue(value);
        } else if (paymentAction == PaymentType.TYPE_REQUEST) {
            sendPaymentRequestWithValue(value);
        }
    }

    private void fetchUserFromAddress(final String remoteUserAddress) {
        if (this.getUserSubscription != null) {
            this.getUserSubscription.unsubscribe();
        }

        this.getUserSubscription =
                BaseApplication
                        .get()
                        .getTokenManager()
                        .getUserManager()
                        .getUserFromAddress(remoteUserAddress)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(this::handleUserLoaded, this::handleUserFetchFailed);
    }

    private void handleUserLoaded(final User user) {
        this.remoteUser = user;
        if (this.remoteUser != null) {
            processIntentData();
        }
    }

    private void handleUserFetchFailed(final Throwable throwable) {
        Toast.makeText(BaseApplication.get(), R.string.error__app_loading, Toast.LENGTH_LONG).show();
        if (this.activity != null) {
            this.activity.finish();
        }
    }

    private void updateUiFromRemoteUser() {
        initToolbar(this.remoteUser);
        initChatMessageStore(this.remoteUser);
        initLoadingSpinner(this.remoteUser);
    }

    private void initToolbar(final User remoteUser) {
        this.activity.getBinding().title.setText(remoteUser.getDisplayName());
        this.activity.getBinding().closeButton.setOnClickListener(this.backButtonClickListener);
        this.activity.setSupportActionBar(this.activity.getBinding().toolbar);
        this.activity.getSupportActionBar().setDisplayShowTitleEnabled(false);
        Glide.with(this.activity.getBinding().avatar.getContext())
                .load(remoteUser.getAvatar())
                .into(this.activity.getBinding().avatar);
    }

    private void initChatMessageStore(final User remoteUser) {
        if (this.conversationStore != null) {
            return;
        }

        ChatNotificationManager.suppressNotificationsForConversation(remoteUser.getOwnerAddress());
        this.conversationStore = new ConversationStore();
        final Pair<PublishSubject<SofaMessage>, PublishSubject<SofaMessage>> observables
                = this.conversationStore.registerForChanges(remoteUser.getOwnerAddress());
        observables.first
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this.handleNewMessage);
        observables.second
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this.handleUpdatedMessage);
        this.conversationStore.loadByAddress(remoteUser.getOwnerAddress())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this.handleConversationLoaded);
    }

    private void initLoadingSpinner(final User remoteUser) {
        if (this.activity == null) return;
        this.activity.getBinding().loadingViewContainer.setVisibility(remoteUser == null ? View.VISIBLE : View.GONE);
        if (remoteUser == null) {
            final Animation rotateAnimation = AnimationUtils.loadAnimation(this.activity, R.anim.rotate);
            this.activity.getBinding().loadingView.startAnimation(rotateAnimation);
        } else {
            this.activity.getBinding().loadingView.clearAnimation();
        }
    }

    private void initLayoutManager() {
        this.layoutManager = new SpeedyLinearLayoutManager(this.activity);
        this.activity.getBinding().messagesList.setLayoutManager(this.layoutManager);
    }

    private void initControlView() {
        this.activity.getBinding().controlView.setOnSizeChangedListener(this.controlViewSizeChangedListener);
    }

    private ControlRecyclerView.OnSizeChangedListener controlViewSizeChangedListener = height -> setPadding(height);

    private void initAdapterAnimation() {
        final SlideUpAnimator anim;
        if (Build.VERSION.SDK_INT >= 21) {
            anim = new SlideUpAnimator(new PathInterpolator(0.33f, 0.78f, 0.3f, 1));
        } else {
            anim = new SlideUpAnimator(new DecelerateInterpolator());
        }
        anim.setAddDuration(400);
        this.activity.getBinding().messagesList.setItemAnimator(anim);
    }

    private void initRecyclerView() {
        this.messageAdapter.notifyDataSetChanged();
        this.activity.getBinding().messagesList.setAdapter(this.messageAdapter);

        // Hack to scroll to bottom when keyboard rendered
        this.activity.getBinding().messagesList.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
            if (bottom < oldBottom) {
                activity.getBinding().messagesList.postDelayed(() -> activity.getBinding().messagesList.smoothScrollToPosition(messageAdapter.getItemCount() - 1), 100);
            }
        });

        this.activity.getBinding().messagesList.getLayoutManager().scrollToPosition(this.lastVisibleMessagePosition);
        updateEmptyState();
    }

    private void sendCommandMessage(final Control control) {
        final Command command = new Command()
                .setBody(control.getLabel())
                .setValue(control.getValue());
        final String commandPayload = adapters.toJson(command);
        final SofaMessage sofaCommandMessage = new SofaMessage().makeNew(true, commandPayload);

        BaseApplication.get()
                .getTokenManager()
                .getSofaMessageManager()
                .sendAndSaveMessage(remoteUser, sofaCommandMessage);
    }

    private void initButtons() {
        this.activity.getBinding().sendButton.setOnClickListener(this.sendButtonClicked);
        this.activity.getBinding().balanceBar.setOnRequestClicked(this.requestButtonClicked);
        this.activity.getBinding().balanceBar.setOnPayClicked(this.payButtonClicked);
        this.activity.getBinding().controlView.setOnControlClickedListener(this.controlClicked);
    }

    private final ControlView.OnControlClickedListener controlClicked = new ControlView.OnControlClickedListener() {
        @Override
        public void onControlClicked(Control control) {
            activity.getBinding().controlView.hideView();
            removePadding();
            sendCommandMessage(control);
        }
    };

    private final OnSingleClickListener sendButtonClicked = new OnSingleClickListener() {
        @Override
        public void onSingleClick(final View v) {
            if (userInputInvalid()) {
                return;
            }

            final String userInput = activity.getBinding().userInput.getText().toString();
            final Message sofaMessage = new Message().setBody(userInput);
            final String messageBody = adapters.toJson(sofaMessage);
            final SofaMessage message = new SofaMessage().makeNew(true, messageBody);
            BaseApplication.get()
                    .getTokenManager()
                    .getSofaMessageManager()
                    .sendAndSaveMessage(remoteUser, message);

            activity.getBinding().userInput.setText(null);
        }

        private boolean userInputInvalid() {
            return activity.getBinding().userInput.getText().toString().trim().length() == 0;
        }
    };

    private final OnSingleClickListener requestButtonClicked = new OnSingleClickListener() {
        @Override
        public void onSingleClick(final View v) {
            final Intent intent = new Intent(activity, AmountActivity.class)
                    .putExtra(AmountActivity.VIEW_TYPE, PaymentType.TYPE_REQUEST);
            activity.startActivityForResult(intent, REQUEST_RESULT_CODE);
        }
    };

    private final OnSingleClickListener payButtonClicked = new OnSingleClickListener() {
        @Override
        public void onSingleClick(final View v) {
            final Intent intent = new Intent(activity, AmountActivity.class)
                    .putExtra(AmountActivity.VIEW_TYPE, PaymentType.TYPE_REQUEST);
            activity.startActivityForResult(intent, PAY_RESULT_CODE);
        }
    };

    private final OnItemClickListener<SofaMessage> handlePaymentRequestApprove = new OnItemClickListener<SofaMessage>() {
        @Override
        public void onItemClick(final SofaMessage existingMessage) {
            final PaymentRequest request = updatePaymentRequestState(existingMessage, PaymentRequest.ACCEPTED);
            sendPaymentWithValue(request.getValue());
        }
    };

    private final OnItemClickListener<SofaMessage> handlePaymentRequestReject = new OnItemClickListener<SofaMessage>() {
        @Override
        public void onItemClick(final SofaMessage existingMessage) {
            updatePaymentRequestState(existingMessage, PaymentRequest.REJECTED);
        }
    };

    private PaymentRequest updatePaymentRequestState(
            final SofaMessage existingMessage,
            final @PaymentRequest.State int newState) {
        try {
            final PaymentRequest paymentRequest = adapters
                    .txRequestFrom(existingMessage.getPayload())
                    .setState(newState);

            final String updatedPayload = adapters.toJson(paymentRequest);
            final SofaMessage updatedMessage = new SofaMessage(existingMessage).setPayload(updatedPayload);

            conversationStore.updateMessage(remoteUser, updatedMessage);
            return paymentRequest;

        } catch (final IOException ex) {
            LogUtil.e(ChatPresenter.this.getClass(), "Error change Payment Request state. " + ex);
        }
        return null;
    }

    private final OnNextSubscriber<SofaMessage> handleNewMessage = new OnNextSubscriber<SofaMessage>() {
        @Override
        public void onNext(final SofaMessage sofaMessage) {
            if (isInitRequest(sofaMessage)) {
                sendInitMessage(sofaMessage);
                return;
            }

            setControlView(sofaMessage);
            messageAdapter.addMessage(sofaMessage);
            updateEmptyState();
            tryScrollToBottom(true);
            playNewMessageSound(sofaMessage.isSentByLocal());
            handleKeyboardVisibility(sofaMessage);
        }
    };

    private void playNewMessageSound(final boolean sentByLocal) {
        if (sentByLocal) {
            SoundManager.getInstance().playSound(SoundManager.SEND_MESSAGE);
        } else {
            SoundManager.getInstance().playSound(SoundManager.RECEIVE_MESSAGE);
        }
    }

    private void handleKeyboardVisibility(final SofaMessage sofaMessage) {
        final boolean viewIsNull = this.activity == null || this.activity.getBinding().userInput == null;
        if (viewIsNull || sofaMessage.isSentByLocal()) {
            return;
        }

        try {
            final Message message = this.adapters.messageFrom(sofaMessage.getPayload());
            if (message.shouldShowKeyboard()) {
                showKeyboard();
            } else {
                hideKeyboard();
            }

        } catch (IOException e) {
            LogUtil.e(getClass(), "Error during handling visibility of keyboard");
        }
    }

    private void showKeyboard() {
        ((InputMethodManager) this.activity.getSystemService(Context.INPUT_METHOD_SERVICE))
                .toggleSoftInputFromWindow(this.activity.getBinding().userInput.getApplicationWindowToken(),
                InputMethodManager.SHOW_FORCED, 0);
    }

    private void hideKeyboard() {
        ((InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE))
                .hideSoftInputFromWindow(activity.getBinding().userInput.getWindowToken(), 0);
    }

    private final OnNextSubscriber<SofaMessage> handleUpdatedMessage = new OnNextSubscriber<SofaMessage>() {
        @Override
        public void onNext(final SofaMessage sofaMessage) {
            messageAdapter.updateMessage(sofaMessage);
        }
    };

    private final OnNextSubscriber<PendingTransaction> handlePendingTransactionChange = new OnNextSubscriber<PendingTransaction>() {
        @Override
        public void onNext(final PendingTransaction pendingTransaction) {
            handleUpdatedMessage.onNext(pendingTransaction.getSofaMessage());
        }
    };

    private boolean isInitRequest(final SofaMessage sofaMessage) {
        final String type = SofaType.createHeader(SofaType.INIT_REQUEST);
        return sofaMessage.getAsSofaMessage().startsWith(type);
    }

    private void sendInitMessage(final SofaMessage sofaMessage) {
        if (userWallet == null || adapters == null) {
            return;
        }

        try {
            final InitRequest initRequest = adapters.initRequestFrom(sofaMessage.getPayload());
            final Init initMessage = new Init().construct(initRequest, this.userWallet.getPaymentAddress());
            final String payload = adapters.toJson(initMessage);
            final SofaMessage newSofaMessage = new SofaMessage().makeNew(false, payload);

            BaseApplication.get()
                    .getTokenManager()
                    .getSofaMessageManager()
                    .sendMessage(remoteUser, newSofaMessage);
        } catch (IOException e) {
            LogUtil.e(getClass(), "IOException " + e);
        }
    }

    private final SingleSuccessSubscriber<Conversation> handleConversationLoaded = new SingleSuccessSubscriber<Conversation>() {
        @Override
        public void onSuccess(final Conversation conversation) {
            if (conversation == null) {
                return;
            }

            final RealmList<SofaMessage> messages = conversation.getAllMessages();
            if (messages.size() > 0) {
                messageAdapter.addMessages(messages);
                forceScrollToBottom();
                updateEmptyState();

                final SofaMessage lastSofaMessage = messages.get(messages.size() - 1);
                setControlView(lastSofaMessage);
            }

            this.unsubscribe();
        }
    };

    private void setControlView(final SofaMessage sofaMessage) {
        if (sofaMessage == null) {
            return;
        }

        try {
            final Message message = adapters.messageFrom(sofaMessage.getPayload());
            final boolean notNullAndNotZero = message.getControls() != null && message.getControls().size() > 0;
            this.activity.getBinding().controlView.hideView();

            if (notNullAndNotZero) {
                this.activity.getBinding().controlView.showControls(message.getControls());
            } else {
                removePadding();
            }
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    private void setPadding(final int height) {
        final int paddingRight = this.activity.getBinding().messagesList.getPaddingRight();
        final int paddingLeft = this.activity.getBinding().messagesList.getPaddingLeft();
        this.activity.getBinding().messagesList.setPadding(paddingLeft, 0 , paddingRight, height);
        this.activity.getBinding().messagesList.scrollToPosition(this.messageAdapter.getItemCount() - 1);
    }

    private void removePadding() {
        final int paddingRight = this.activity.getBinding().messagesList.getPaddingRight();
        final int paddingLeft = this.activity.getBinding().messagesList.getPaddingLeft();
        final int paddingBottom = this.activity.getResources().getDimensionPixelSize(R.dimen.message_list_bottom_padding);
        this.activity.getBinding().messagesList.setPadding(paddingLeft, 0 , paddingRight, paddingBottom);
    }

    private void tryScrollToBottom(final boolean animate) {
        if (this.activity == null || this.layoutManager == null || this.messageAdapter.getItemCount() == 0) {
            return;
        }

        // Only animate if we're already near the bottom
        if (this.layoutManager.findLastVisibleItemPosition() < this.messageAdapter.getItemCount() - 3) {
            return;
        }

        if (animate) {
            this.activity.getBinding().messagesList.smoothScrollToPosition(this.messageAdapter.getItemCount() - 1);
        } else {
            forceScrollToBottom();
        }
    }

    private void forceScrollToBottom() {
        this.activity.getBinding().messagesList.scrollToPosition(this.messageAdapter.getItemCount() - 1);
    }

    private void updateEmptyState() {
        // Hide empty state if we have some content
        final boolean showingEmptyState = this.activity.getBinding().emptyStateSwitcher.getCurrentView().getId() == this.activity.getBinding().emptyState.getId();
        final boolean shouldShowEmptyState = this.messageAdapter.getItemCount() == 0;

        if (shouldShowEmptyState && !showingEmptyState) {
            this.activity.getBinding().emptyStateSwitcher.showPrevious();
        } else if (!shouldShowEmptyState && showingEmptyState) {
            this.activity.getBinding().emptyStateSwitcher.showNext();
        }
    }

    @Override
    public void onViewDetached() {
        if (this.notEnoughFundsDialog != null) {
            this.notEnoughFundsDialog.dismiss();
        }
        this.lastVisibleMessagePosition = this.layoutManager.findLastVisibleItemPosition();
        this.activity = null;
    }

    @Override
    public void onViewDestroyed() {
        if (this.messageAdapter != null) {
            this.messageAdapter = null;
        }

        if (this.getUserSubscription != null) {
            this.getUserSubscription.unsubscribe();
            this.getUserSubscription = null;
        }

        this.handleNewMessage.unsubscribe();
        this.handleUpdatedMessage.unsubscribe();

        if (this.conversationStore != null) {
            this.conversationStore.stopListeningForChanges();
            this.conversationStore = null;
        }

        ChatNotificationManager.stopNotificationSuppresion();
        this.activity = null;
    }

    private final View.OnClickListener backButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(final View v) {
            hideKeyboard();
            activity.onBackPressed();
        }
    };

    public void handleActivityResult(final ActivityResultHolder resultHolder) {
        if (resultHolder.getResultCode() != Activity.RESULT_OK) {
            return;
        }

        if (resultHolder.getRequestCode() == REQUEST_RESULT_CODE) {
            final String value = resultHolder.getIntent().getStringExtra(AmountPresenter.INTENT_EXTRA__ETH_AMOUNT);
            sendPaymentRequestWithValue(value);
        } else if(resultHolder.getRequestCode() == PAY_RESULT_CODE) {
            final String value = resultHolder.getIntent().getStringExtra(AmountPresenter.INTENT_EXTRA__ETH_AMOUNT);
            sendPaymentWithValue(value);
        }
    }

    private void sendPaymentWithValue(final String value) {
        BaseApplication.get()
                .getTokenManager()
                .getBalanceManager()
                .getBalanceObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new OnNextSubscriber<Balance>() {
                    @Override
                    public void onNext(Balance balance) {
                        this.unsubscribe();
                        trySendPayment(balance, value);
                    }
                });
    }

    private void trySendPayment(final Balance balance, final String value) {
        final BigInteger paymentAmount = TypeConverter.StringHexToBigInteger(value);
        final BigInteger localAmount = balance.getConfirmedBalance();
        if (localAmount.compareTo(paymentAmount) == -1) {
            showNotEnoughFundsDialog();
            return;
        }

        final Payment payment = new Payment()
                .setValue(value)
                .setFromAddress(userWallet.getPaymentAddress())
                .setToAddress(remoteUser.getPaymentAddress());

        BaseApplication.get()
                .getTokenManager()
                .getTransactionManager()
                .sendPayment(remoteUser, payment);
    }

    private void showNotEnoughFundsDialog() {
        if (this.activity == null) {
            return;
        }

        this.notEnoughFundsDialog = new AlertDialog.Builder(this.activity)
                .setTitle(R.string.not_enough_funds_title)
                .setMessage(R.string.not_enough_funds_message)
                .setPositiveButton(R.string.got_it, (dialog, which) -> {
                    dialog.dismiss();
                })
                .show();
    }

    private void sendPaymentRequestWithValue(final String value) {
        final PaymentRequest request = new PaymentRequest()
                .setDestinationAddress(userWallet.getPaymentAddress())
                .setValue(value);
        final String messageBody = this.adapters.toJson(request);
        final SofaMessage message = new SofaMessage().makeNew(true, messageBody);

        BaseApplication
                .get()
                .getTokenManager()
                .getSofaMessageManager()
                .sendAndSaveMessage(remoteUser, message);
    }

    public void handleActionMenuClicked(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.rate: {
                Toast.makeText(this.activity, "Not implemented yet", Toast.LENGTH_SHORT).show();
                break;
            }
            case R.id.view_profile: {
                viewProfile();
                break;
            }
            default: {
                LogUtil.d(getClass(), "Not valid menu item");
            }
        }
    }

    private void viewProfile() {
        if (this.remoteUser == null) {
            return;
        }

        final Intent intent = new Intent(this.activity, ViewUserActivity.class)
                .putExtra(ViewUserActivity.EXTRA__USER_ADDRESS, this.remoteUser.getOwnerAddress());
        this.activity.startActivity(intent);
    }
}
