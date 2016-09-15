package com.bakkenbaeck.toshi.presenter;

import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.bakkenbaeck.toshi.R;
import com.bakkenbaeck.toshi.model.ActivityResultHolder;
import com.bakkenbaeck.toshi.model.ChatMessage;
import com.bakkenbaeck.toshi.network.ws.model.Payment;
import com.bakkenbaeck.toshi.presenter.store.ChatMessageStore;
import com.bakkenbaeck.toshi.util.OnCompletedObserver;
import com.bakkenbaeck.toshi.util.OnNextObserver;
import com.bakkenbaeck.toshi.view.BaseApplication;
import com.bakkenbaeck.toshi.view.activity.ChatActivity;
import com.bakkenbaeck.toshi.view.activity.VideoActivity;
import com.bakkenbaeck.toshi.view.activity.WithdrawActivity;
import com.bakkenbaeck.toshi.view.adapter.MessageAdapter;

import java.math.BigInteger;

import io.realm.Realm;
import rx.Subscriber;

import static android.app.Activity.RESULT_OK;

public final class ChatPresenter implements Presenter<ChatActivity> {

    private final int VIDEO_REQUEST_CODE = 1;
    private final int WITHDRAW_REQUEST_CODE = 2;

    private ChatActivity activity;
    private MessageAdapter messageAdapter;
    private boolean firstViewAttachment = true;
    private boolean isShowingAnotherOneButton;
    private ChatMessageStore chatMessageStore;

    @Override
    public void onViewAttached(final ChatActivity activity) {
        this.activity = activity;
        initToolbar();

        if (firstViewAttachment) {
            firstViewAttachment = false;
            initLongLivingObjects();
        }
        initShortLivingObjects();

        // Refresh state
        this.messageAdapter.notifyDataSetChanged();
        refreshAnotherOneButtonState();
        scrollToBottom(false);
    }

    private void initLongLivingObjects() {
        this.chatMessageStore = new ChatMessageStore();
        this.chatMessageStore.getEmptySetObservable().subscribe(this.noStoredChatMessages);
        this.chatMessageStore.getNewMessageObservable().subscribe(this.newChatMessage);
        this.chatMessageStore.getUnwatchedVideoObservable().subscribe(this.unwatchedVideo);
        BaseApplication.get().getOfflineBalance().getObservable().subscribe(this.newBalanceSubscriber);
        BaseApplication.get().getOfflineBalance().getUpsellObservable().subscribe(this.upsellSubscriber);
        BaseApplication.get().getSocketObservables().getPaymentObservable().subscribe(this.newPaymentSubscriber);

        this.messageAdapter = new MessageAdapter();
        registerMessageClickedObservable();

        this.chatMessageStore.load();
    }

    private void initShortLivingObjects() {
        this.activity.getBinding().messagesList.setAdapter(this.messageAdapter);
    }

    private void initToolbar() {
        final String title = this.activity.getResources().getString(R.string.chat__title);
        final Toolbar toolbar = this.activity.getBinding().toolbar;
        this.activity.setSupportActionBar(toolbar);
        this.activity.getSupportActionBar().setTitle(title);
    }

    private final OnCompletedObserver<Void> noStoredChatMessages = new OnCompletedObserver<Void>() {
        @Override
        public void onCompleted() {
            showWelcomeMessage();
        }
    };

    private final OnNextObserver<ChatMessage> newChatMessage = new OnNextObserver<ChatMessage>() {
        @Override
        public void onNext(final ChatMessage chatMessage) {
            messageAdapter.addMessage(chatMessage);
        }
    };

    private final OnNextObserver<Boolean> unwatchedVideo = new OnNextObserver<Boolean>() {
        @Override
        public void onNext(final Boolean hasUnwatchedVideo) {
            if (!hasUnwatchedVideo) {
                promptNewVideo();
            }
        }
    };

    private void showWelcomeMessage() {
        final ChatMessage response = new ChatMessage().makeRemoteMessageWithText(this.activity.getResources().getString(R.string.chat__welcome_message));
        displayMessage(response);
        showAVideo();
    }

    private void showAVideo() {
        final ChatMessage video = new ChatMessage().makeRemoteVideoMessage();
        displayMessage(video, 700);
    }

    private void showVideoRequestMessage() {
        final ChatMessage message = new ChatMessage().makeLocalMessageWithText(this.activity.getResources().getString(R.string.chat__request_video_message));
        displayMessage(message);
    }

    private void displayMessage(final ChatMessage chatMessage, final int delay) {
        final Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                chatMessageStore.save(chatMessage);
                scrollToBottom(true);
            }
        }, delay);

    }

    private void displayMessage(final ChatMessage chatMessage) {
        displayMessage(chatMessage, 200);
    }

    private final OnNextObserver<BigInteger> newBalanceSubscriber = new OnNextObserver<BigInteger>() {
        @Override
        public void onNext(final BigInteger newBalance) {
            if (activity != null && newBalance != null) {
                activity.getBinding().balanceBar.setBalance(newBalance.toString());
            }
        }
    };

    private final OnNextObserver<Payment> newPaymentSubscriber = new OnNextObserver<Payment>() {
        @Override
        public void onNext(final Payment payment) {
            handleNewPayment(payment);
        }
    };

    private void handleNewPayment(final Payment payment) {
        final String amount = payment.getAmount().toString();
        final String message = String.format(BaseApplication.get().getResources().getString(R.string.chat__currency_earned), amount);
        final ChatMessage response = new ChatMessage().makeRemoteMessageWithText(message);
        displayMessage(response, 500);
    }

    private final OnCompletedObserver<Void> upsellSubscriber = new OnCompletedObserver<Void>() {
        @Override
        public void onCompleted() {
            final ChatMessage message = new ChatMessage().makeRemoteWithdrawMessage();
            displayMessage(message);
        }
    };

    private void withdrawAmountFromAddress(final BigInteger amount, final String walletAddress) {
        final String message = String.format(this.activity.getResources().getString(R.string.chat__withdraw_to_address), amount.toString(), walletAddress);
        final ChatMessage response = new ChatMessage().makeRemoteMessageWithText(message);
        displayMessage(response, 500);
        // TODO
        // offlineBalance.subtract(amount);
    }

    private void scrollToBottom(final boolean animate) {
        if (this.activity != null && this.messageAdapter.getItemCount() > 0) {
            if (animate) {
                this.activity.getBinding().messagesList.smoothScrollToPosition(this.messageAdapter.getItemCount() - 1);
            } else {
                this.activity.getBinding().messagesList.scrollToPosition(this.messageAdapter.getItemCount() - 1);
            }
        }
    }

    private void refreshAnotherOneButtonState() {
        this.activity.getBinding().buttonAnotherVideo.setVisibility(
                this.isShowingAnotherOneButton
                        ? View.VISIBLE
                        : View.INVISIBLE
        );

        if (this.isShowingAnotherOneButton) {
            this.activity.getBinding().buttonAnotherVideo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View view) {
                    isShowingAnotherOneButton = false;
                    refreshAnotherOneButtonState();
                    showVideoRequestMessage();
                    showAVideo();
                }
            });
        }
    }

    private void promptNewVideo() {
        this.isShowingAnotherOneButton = true;
        refreshAnotherOneButtonState();
    }

    @Override
    public void onViewDetached() {
        this.activity = null;
    }

    private void registerMessageClickedObservable() {
        this.messageAdapter.getPositionClicks().subscribe(this.clicksSubscriber);
    }

    private void unregisterMessageClickedObservable() {
        if (this.clicksSubscriber.isUnsubscribed()) {
            return;
        }
        this.clicksSubscriber.unsubscribe();
    }

    private final Subscriber<Integer> clicksSubscriber = new Subscriber<Integer>() {
        @Override
        public void onCompleted() {}

        @Override
        public void onError(final Throwable e) {}

        @Override
        public void onNext(final Integer clickedPosition) {
            showVideoActivity(clickedPosition);
        }

        private void showVideoActivity(final int clickedPosition) {
            final Intent intent = new Intent(activity, VideoActivity.class);
            intent.putExtra(VideoPresenter.INTENT_CLICKED_POSITION, clickedPosition);
            activity.startActivityForResult(intent, VIDEO_REQUEST_CODE);
        }
    };

    @Override
    public void onViewDestroyed() {
        this.activity = null;
        unregisterMessageClickedObservable();
    }

    public void handleActivityResult(final ActivityResultHolder activityResultHolder) {
        if (activityResultHolder.getResultCode() != RESULT_OK) {
            return;
        }

        if (activityResultHolder.getRequestCode() == VIDEO_REQUEST_CODE) {
            handleVideoCompleted(activityResultHolder);
            return;
        }

        if (activityResultHolder.getRequestCode() == WITHDRAW_REQUEST_CODE) {
            final String address = activityResultHolder.getIntent().getStringExtra(WithdrawPresenter.INTENT_WALLET_ADDRESS);
            final BigInteger amount = (BigInteger) activityResultHolder.getIntent().getSerializableExtra(WithdrawPresenter.INTENT_WITHDRAW_AMOUNT);
            withdrawAmountFromAddress(amount, address);
        }
    }

    private void handleVideoCompleted(final ActivityResultHolder activityResultHolder) {
        markVideoAsWatched(activityResultHolder);
        promptNewVideo();
    }

    private void markVideoAsWatched(final ActivityResultHolder activityResultHolder) {
        final int clickedPosition = activityResultHolder.getIntent().getIntExtra(VideoPresenter.INTENT_CLICKED_POSITION, 0);
        final ChatMessage clickedMessage = this.messageAdapter.getItemAt(clickedPosition);
        Realm.getDefaultInstance().beginTransaction();
        clickedMessage.markAsWatched();
        Realm.getDefaultInstance().commitTransaction();
        this.messageAdapter.notifyItemChanged(clickedPosition);
    }

    public void handleWithdrawClicked() {
        final Intent intent = new Intent(this.activity, WithdrawActivity.class);
        this.activity.startActivityForResult(intent, WITHDRAW_REQUEST_CODE);
        this.activity.overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
    }
}
