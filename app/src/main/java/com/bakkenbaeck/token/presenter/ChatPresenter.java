package com.bakkenbaeck.token.presenter;

import android.content.Intent;
import android.os.Build;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.PathInterpolator;

import com.bakkenbaeck.token.crypto.HDWallet;
import com.bakkenbaeck.token.model.local.ActivityResultHolder;
import com.bakkenbaeck.token.model.local.ChatMessage;
import com.bakkenbaeck.token.model.local.PendingTransaction;
import com.bakkenbaeck.token.model.local.User;
import com.bakkenbaeck.token.model.sofa.Command;
import com.bakkenbaeck.token.model.sofa.Control;
import com.bakkenbaeck.token.model.sofa.Init;
import com.bakkenbaeck.token.model.sofa.InitRequest;
import com.bakkenbaeck.token.model.sofa.Message;
import com.bakkenbaeck.token.model.sofa.Payment;
import com.bakkenbaeck.token.model.sofa.PaymentRequest;
import com.bakkenbaeck.token.model.sofa.SofaAdapters;
import com.bakkenbaeck.token.model.sofa.SofaType;
import com.bakkenbaeck.token.presenter.store.ChatMessageStore;
import com.bakkenbaeck.token.presenter.store.PendingTransactionStore;
import com.bakkenbaeck.token.util.LogUtil;
import com.bakkenbaeck.token.util.OnNextSubscriber;
import com.bakkenbaeck.token.util.OnSingleClickListener;
import com.bakkenbaeck.token.util.SingleSuccessSubscriber;
import com.bakkenbaeck.token.view.Animation.SlideUpAnimator;
import com.bakkenbaeck.token.view.BaseApplication;
import com.bakkenbaeck.token.view.activity.AmountActivity;
import com.bakkenbaeck.token.view.activity.ChatActivity;
import com.bakkenbaeck.token.view.adapter.MessageAdapter;
import com.bakkenbaeck.token.view.custom.ControlView;
import com.bakkenbaeck.token.view.custom.SpeedyLinearLayoutManager;

import java.io.IOException;

import io.realm.RealmResults;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static android.app.Activity.RESULT_OK;

public final class ChatPresenter implements
        Presenter<ChatActivity> {

    private static final int REQUEST_RESULT_CODE = 1;
    private static final int PAY_RESULT_CODE = 2;

    private ChatActivity activity;
    private MessageAdapter messageAdapter;
    private boolean firstViewAttachment = true;
    private ChatMessageStore chatMessageStore;
    private PendingTransactionStore pendingTransactionStore;
    private User remoteUser;
    private SpeedyLinearLayoutManager layoutManager;
    private SofaAdapters adapters;
    private HDWallet userWallet;

    public void setRemoteUser(final User remoteUser) {
        this.remoteUser = remoteUser;
    }

    @Override
    public void onViewAttached(final ChatActivity activity) {
        this.activity = activity;
        initToolbar();

        if (firstViewAttachment) {
            firstViewAttachment = false;
            initLongLivingObjects();
        }
        initShortLivingObjects();
    }

    private void initToolbar() {
        this.activity.getBinding().title.setText(this.remoteUser.getUsername());
        this.activity.getBinding().avatar.setImageBitmap(this.remoteUser.getImage());
        this.activity.getBinding().closeButton.setOnClickListener(this.backButtonClickListener);
    }

    private void initLongLivingObjects() {
        this.messageAdapter = new MessageAdapter();
        this.adapters = new SofaAdapters();

        initChatMessageStore();
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

    private void initPendingTransactionStore() {
        this.pendingTransactionStore = new PendingTransactionStore();
        this.pendingTransactionStore
                .getPendingTransactionObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this.handlePendingTransactionChange);
    }

    private void initChatMessageStore() {
        this.chatMessageStore = new ChatMessageStore();
        this.chatMessageStore.getNewMessageObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this.handleNewMessage);
        this.chatMessageStore.getUpdatedMessageObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this.handleUpdatedMessage);
        this.chatMessageStore.load(this.remoteUser.getOwnerAddress())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this.handleLoadMessages);
    }

    private void initShortLivingObjects() {
        initLayoutManager();
        initAdapterAnimation();
        initRecyclerView();
        initButtons();
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
        this.activity.getBinding().messagesList.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(final View v,
                                       final int left, final int top, final int right, final int bottom,
                                       final int oldLeft, final int oldTop, final int oldRight, final int oldBottom) {
                if (bottom < oldBottom) {
                    activity.getBinding().messagesList.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            activity.getBinding().messagesList.smoothScrollToPosition(messageAdapter.getItemCount() - 1);
                        }
                    }, 100);
                }
            }
        });

        updateEmptyState();
    }

    private void sendCommandMessage(final Control control) {
        final Command command = new Command()
                .setBody(control.getLabel())
                .setValue(control.getValue());
        final String commandPayload = adapters.toJson(command);

        final ChatMessage sofaCommandMessage = new ChatMessage()
                .makeNew(remoteUser.getOwnerAddress(), true, commandPayload);

        BaseApplication.get()
                .getTokenManager()
                .getChatMessageManager()
                .sendMessage(sofaCommandMessage);
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
            final ChatMessage message = new ChatMessage().makeNew(remoteUser.getOwnerAddress(), true, messageBody);
            BaseApplication.get()
                    .getTokenManager()
                    .getChatMessageManager()
                    .sendAndSaveMessage(message);

            activity.getBinding().userInput.setText(null);
        }

        private boolean userInputInvalid() {
            return activity.getBinding().userInput.getText().toString().trim().length() == 0;
        }
    };

    private final OnSingleClickListener requestButtonClicked = new OnSingleClickListener() {
        @Override
        public void onSingleClick(final View v) {
            final Intent intent = new Intent(activity, AmountActivity.class);
            activity.startActivityForResult(intent, REQUEST_RESULT_CODE);
        }
    };

    private final OnSingleClickListener payButtonClicked = new OnSingleClickListener() {
        @Override
        public void onSingleClick(final View v) {
            final Intent intent = new Intent(activity, AmountActivity.class);
            activity.startActivityForResult(intent, PAY_RESULT_CODE);
        }
    };

    private final OnNextSubscriber<ChatMessage> handleNewMessage = new OnNextSubscriber<ChatMessage>() {
        @Override
        public void onNext(final ChatMessage chatMessage) {
            if (!messageBelongsInThisConversation(chatMessage)) {
                return;
            }

            if (isInitRequest(chatMessage)) {
                sendInitMessage(chatMessage);
                return;
            }

            setControlView(chatMessage);
            messageAdapter.addMessage(chatMessage);
            updateEmptyState();
            tryScrollToBottom(true);
        }
    };

    private final OnNextSubscriber<ChatMessage> handleUpdatedMessage = new OnNextSubscriber<ChatMessage>() {
        @Override
        public void onNext(final ChatMessage chatMessage) {
            if (!messageBelongsInThisConversation(chatMessage)) {
                return;
            }

            messageAdapter.updateMessage(chatMessage);
        }
    };

    private final OnNextSubscriber<PendingTransaction> handlePendingTransactionChange = new OnNextSubscriber<PendingTransaction>() {
        @Override
        public void onNext(final PendingTransaction pendingTransaction) {
            handleUpdatedMessage.onNext(pendingTransaction.getChatMessage());
        }
    };

    private boolean messageBelongsInThisConversation(final ChatMessage chatMessage) {
        return chatMessage.getConversationId().equals(remoteUser.getOwnerAddress());
    }

    private boolean isInitRequest(final ChatMessage chatMessage) {
        final String type = SofaType.createHeader(SofaType.INIT_REQUEST);
        return chatMessage.getAsSofaMessage().startsWith(type);
    }

    private void sendInitMessage(final ChatMessage chatMessage) {
        if (userWallet == null || adapters == null) {
            return;
        }

        try {
            final InitRequest initRequest = adapters.initRequestFrom(chatMessage.getPayload());
            final Init initMessage = new Init().construct(initRequest, this.userWallet.getOwnerAddress());
            final String payload = adapters.toJson(initMessage);
            final ChatMessage newChatMessage = new ChatMessage()
                    .makeNew(chatMessage.getConversationId(), false, payload);

            BaseApplication.get()
                    .getTokenManager()
                    .getChatMessageManager()
                    .sendMessage(newChatMessage);
        } catch (IOException e) {
            LogUtil.e(getClass(), "IOException " + e);
        }
    }

    private final SingleSuccessSubscriber<RealmResults<ChatMessage>> handleLoadMessages = new SingleSuccessSubscriber<RealmResults<ChatMessage>>() {
        @Override
        public void onSuccess(final RealmResults<ChatMessage> chatMessages) {
            if (chatMessages.size() > 0) {
                messageAdapter.addMessages(chatMessages);
                forceScrollToBottom();
                updateEmptyState();

                final ChatMessage lastChatMessage = chatMessages.get(chatMessages.size() - 1);
                setControlView(lastChatMessage);
            }

            this.unsubscribe();
        }
    };

    private void setControlView(final ChatMessage chatMessage) {
        if (chatMessage == null) {
            return;
        }

        try {
            final Message message = adapters.messageFrom(chatMessage.getPayload());
            final boolean notNullAndNotZero = message.getControls() != null && message.getControls().size() > 0;
            this.activity.getBinding().controlView.hideView();

            if (notNullAndNotZero) {
                this.activity.getBinding().controlView.showControls(message.getControls());
            }
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    private void tryScrollToBottom(final boolean animate) {
        if (this.activity == null || this.layoutManager == null || this.messageAdapter.getItemCount() == 0) {
            return;
        }

        // Only animate if we're already near the bottom
        if (this.layoutManager.findLastVisibleItemPosition() < this.messageAdapter.getItemCount() - 2) {
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
        this.activity = null;
    }

    @Override
    public void onViewDestroyed() {
        if (this.messageAdapter != null) {
            this.messageAdapter = null;
        }
        this.handleNewMessage.unsubscribe();
        this.handleUpdatedMessage.unsubscribe();
        this.chatMessageStore = null;
        this.activity = null;
    }

    private final View.OnClickListener backButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(final View v) {
            activity.onBackPressed();
        }
    };

    public void handleActivityResult(final ActivityResultHolder resultHolder) {
        if (resultHolder.getResultCode() != RESULT_OK) {
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
        final Payment payment = new Payment()
                .setValue(value)
                .setOwnerAddress(remoteUser.getOwnerAddress())
                .setToAddress(remoteUser.getPaymentAddress());

        BaseApplication.get()
                .getTokenManager()
                .getTransactionManager()
                .sendPayment(payment);
    }

    private void sendPaymentRequestWithValue(final String value) {
        final PaymentRequest request = new PaymentRequest()
                .setDestinationAddress(userWallet.getPaymentAddress())
                .setValue(value);
        final String messageBody = this.adapters.toJson(request);
        final ChatMessage message = new ChatMessage().makeNew(
                remoteUser.getOwnerAddress(),
                true,
                messageBody);

        BaseApplication
                .get()
                .getTokenManager()
                .getChatMessageManager()
                .sendAndSaveMessage(message);
    }
}
