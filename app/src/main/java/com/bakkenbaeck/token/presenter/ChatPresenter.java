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
import com.bakkenbaeck.token.model.local.ActivityResultHolder;
import com.bakkenbaeck.token.model.local.Conversation;
import com.bakkenbaeck.token.model.local.SofaMessage;
import com.bakkenbaeck.token.model.local.User;
import com.bakkenbaeck.token.model.sofa.Command;
import com.bakkenbaeck.token.model.sofa.Control;
import com.bakkenbaeck.token.model.sofa.Init;
import com.bakkenbaeck.token.model.sofa.InitRequest;
import com.bakkenbaeck.token.model.sofa.Message;
import com.bakkenbaeck.token.model.sofa.PaymentRequest;
import com.bakkenbaeck.token.model.sofa.SofaAdapters;
import com.bakkenbaeck.token.model.sofa.SofaType;
import com.bakkenbaeck.token.util.LogUtil;
import com.bakkenbaeck.token.util.OnSingleClickListener;
import com.bakkenbaeck.token.util.PaymentType;
import com.bakkenbaeck.token.util.SoundManager;
import com.bakkenbaeck.token.view.Animation.SlideUpAnimator;
import com.bakkenbaeck.token.view.BaseApplication;
import com.bakkenbaeck.token.view.activity.AmountActivity;
import com.bakkenbaeck.token.view.activity.ChatActivity;
import com.bakkenbaeck.token.view.activity.ViewUserActivity;
import com.bakkenbaeck.token.view.adapter.MessageAdapter;
import com.bakkenbaeck.token.view.custom.SpeedyLinearLayoutManager;
import com.bakkenbaeck.token.view.notification.ChatNotificationManager;
import com.bumptech.glide.Glide;

import java.io.IOException;
import java.util.List;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;


public final class ChatPresenter implements
        Presenter<ChatActivity> {

    private static final int REQUEST_RESULT_CODE = 1;
    private static final int PAY_RESULT_CODE = 2;

    private ChatActivity activity;
    private MessageAdapter messageAdapter;
    private User remoteUser;
    private SpeedyLinearLayoutManager layoutManager;
    private SofaAdapters adapters;
    private HDWallet userWallet;
    private CompositeSubscription subscriptions;
    private Dialog notEnoughFundsDialog;
    private boolean firstViewAttachment = true;
    private int lastVisibleMessagePosition;

    @Override
    public void onViewAttached(final ChatActivity activity) {
        this.activity = activity;

        if (this.firstViewAttachment) {
            this.firstViewAttachment = false;
            initLongLivingObjects();
        }
        initShortLivingObjects();
    }

    private void initLongLivingObjects() {
        this.subscriptions = new CompositeSubscription();
        initMessageAdapter();
        initPendingTransactionStore();
        initSubscribers();
    }

    private void initMessageAdapter() {
        this.adapters = new SofaAdapters();
        this.messageAdapter = new MessageAdapter()
                .addOnPaymentRequestApproveListener(message -> updatePaymentRequestState(message, PaymentRequest.ACCEPTED))
                .addOnPaymentRequestRejectListener(message -> updatePaymentRequestState(message, PaymentRequest.REJECTED));
    }

    private void updatePaymentRequestState(
            final SofaMessage existingMessage,
            final @PaymentRequest.State int newState) {
        BaseApplication
                .get()
                .getTokenManager()
                .getTransactionManager()
                .updatePaymentRequestState(this.remoteUser, existingMessage, newState);
    }

    private void initPendingTransactionStore() {
        final Subscription sub = BaseApplication
                .get()
                .getTokenManager()
                .getTransactionManager()
                .getPendingTransactionObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(pendingTransaction -> handleUpdatedMessage(pendingTransaction.getSofaMessage()));

        this.subscriptions.add(sub);
    }

    private void handleUpdatedMessage(final SofaMessage sofaMessage) {
        this.messageAdapter.updateMessage(sofaMessage);
    }

    private void initSubscribers() {
        final Subscription sub = BaseApplication.get()
                .getTokenManager()
                .getWallet()
                .subscribeOn(Schedulers.io())
                .subscribe(wallet -> this.userWallet = wallet);

        this.subscriptions.add(sub);
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

    private void initLayoutManager() {
        this.layoutManager = new SpeedyLinearLayoutManager(this.activity);
        this.activity.getBinding().messagesList.setLayoutManager(this.layoutManager);
    }

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

    private void initButtons() {
        this.activity.getBinding().sendButton.setOnClickListener(this.sendButtonClicked);
        this.activity.getBinding().balanceBar.setOnRequestClicked(this.requestButtonClicked);
        this.activity.getBinding().balanceBar.setOnPayClicked(this.payButtonClicked);
        this.activity.getBinding().controlView.setOnControlClickedListener(this::handleControlClicked);
    }

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

    private void handleControlClicked(final Control control) {
        this.activity.getBinding().controlView.hideView();
        removePadding();
        sendCommandMessage(control);
    }

    private void removePadding() {
        final int paddingRight = this.activity.getBinding().messagesList.getPaddingRight();
        final int paddingLeft = this.activity.getBinding().messagesList.getPaddingLeft();
        final int paddingBottom = this.activity.getResources().getDimensionPixelSize(R.dimen.message_list_bottom_padding);
        this.activity.getBinding().messagesList.setPadding(paddingLeft, 0 , paddingRight, paddingBottom);
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

    private void initControlView() {
        this.activity.getBinding().controlView.setOnSizeChangedListener(this::setPadding);
    }

    private void setPadding(final int height) {
        final int paddingRight = this.activity.getBinding().messagesList.getPaddingRight();
        final int paddingLeft = this.activity.getBinding().messagesList.getPaddingLeft();
        this.activity.getBinding().messagesList.setPadding(paddingLeft, 0 , paddingRight, height);
        this.activity.getBinding().messagesList.scrollToPosition(this.messageAdapter.getItemCount() - 1);
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

    private void fetchUserFromAddress(final String remoteUserAddress) {
        final Subscription sub =
                BaseApplication
                        .get()
                        .getTokenManager()
                        .getUserManager()
                        .getUserFromAddress(remoteUserAddress)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(this::handleUserLoaded, this::handleUserFetchFailed);

        this.subscriptions.add(sub);
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

    private void sendPaymentWithValue(final String value) {
        BaseApplication.get()
                .getTokenManager()
                .getTransactionManager()
                .sendPayment(remoteUser, value);
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

    private void initToolbar(final User remoteUser) {
        this.activity.getBinding().title.setText(remoteUser.getDisplayName());
        this.activity.getBinding().closeButton.setOnClickListener(this::handleBackButtonClicked);
        this.activity.setSupportActionBar(this.activity.getBinding().toolbar);
        this.activity.getSupportActionBar().setDisplayShowTitleEnabled(false);
        Glide.with(this.activity.getBinding().avatar.getContext())
                .load(remoteUser.getAvatar())
                .into(this.activity.getBinding().avatar);
    }

    private void handleBackButtonClicked(final View v) {
        hideKeyboard();
        this.activity.onBackPressed();
    }

    private void initChatMessageStore(final User remoteUser) {
        ChatNotificationManager.suppressNotificationsForConversation(remoteUser.getOwnerAddress());

        final Pair<PublishSubject<SofaMessage>, PublishSubject<SofaMessage>> observables =
                BaseApplication
                .get()
                .getTokenManager()
                .getSofaMessageManager()
                .registerForConversationChanges(remoteUser.getOwnerAddress());

        final Subscription subConversationLoaded = BaseApplication
                .get()
                .getTokenManager()
                .getSofaMessageManager()
                .loadConversation(remoteUser.getOwnerAddress())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handleConversationLoaded);

        final Subscription subNewMessage = observables.first
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handleNewMessage);

        final Subscription subUpdateMessage = observables.second
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handleUpdatedMessage);

        this.subscriptions.addAll(subConversationLoaded, subNewMessage, subUpdateMessage);
    }

    private void handleConversationLoaded(final Conversation conversation) {
        if (conversation == null) {
            return;
        }

        final List<SofaMessage> messages = conversation.getAllMessages();
        if (messages.size() > 0) {
            this.messageAdapter.addMessages(messages);
            forceScrollToBottom();
            updateEmptyState();

            final SofaMessage lastSofaMessage = messages.get(messages.size() - 1);
            setControlView(lastSofaMessage);
        }
    }

    private void handleNewMessage(final SofaMessage sofaMessage) {
        if (isInitRequest(sofaMessage)) {
            sendInitMessage(sofaMessage);
            return;
        }

        setControlView(sofaMessage);
        this.messageAdapter.addMessage(sofaMessage);
        updateEmptyState();
        tryScrollToBottom(true);
        playNewMessageSound(sofaMessage.isSentByLocal());
        handleKeyboardVisibility(sofaMessage);
    }

    private boolean isInitRequest(final SofaMessage sofaMessage) {
        final String type = SofaType.createHeader(SofaType.INIT_REQUEST);
        return sofaMessage.getAsSofaMessage().startsWith(type);
    }

    private void sendInitMessage(final SofaMessage sofaMessage) {
        if (this.userWallet == null || this.adapters == null) {
            return;
        }

        try {
            final InitRequest initRequest = this.adapters.initRequestFrom(sofaMessage.getPayload());
            final Init initMessage = new Init().construct(initRequest, this.userWallet.getPaymentAddress());
            final String payload = this.adapters.toJson(initMessage);
            final SofaMessage newSofaMessage = new SofaMessage().makeNew(false, payload);

            BaseApplication.get()
                    .getTokenManager()
                    .getSofaMessageManager()
                    .sendMessage(this.remoteUser, newSofaMessage);
        } catch (IOException e) {
            LogUtil.e(getClass(), "IOException " + e);
        }
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

    private void forceScrollToBottom() {
        this.activity.getBinding().messagesList.scrollToPosition(this.messageAdapter.getItemCount() - 1);
    }

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
        this.messageAdapter = null;
        this.subscriptions.clear();
        stopListeningForConversationChanges();
        ChatNotificationManager.stopNotificationSuppresion();
        this.activity = null;
    }

    private void stopListeningForConversationChanges() {
        BaseApplication
                .get()
                .getTokenManager()
                .getSofaMessageManager()
                .stopListeningForConversationChanges();
    }
}