package com.bakkenbaeck.toshi.presenter;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.bakkenbaeck.toshi.R;
import com.bakkenbaeck.toshi.model.ActivityResultHolder;
import com.bakkenbaeck.toshi.model.ChatMessage;
import com.bakkenbaeck.toshi.model.OfflineBalance;
import com.bakkenbaeck.toshi.model.User;
import com.bakkenbaeck.toshi.network.ws.model.Payment;
import com.bakkenbaeck.toshi.presenter.store.ChatStore;
import com.bakkenbaeck.toshi.util.LogUtil;
import com.bakkenbaeck.toshi.util.OnCompletedObserver;
import com.bakkenbaeck.toshi.view.BaseApplication;
import com.bakkenbaeck.toshi.view.activity.ChatActivity;
import com.bakkenbaeck.toshi.view.activity.VideoActivity;
import com.bakkenbaeck.toshi.view.activity.WithdrawActivity;
import com.bakkenbaeck.toshi.view.adapter.MessageAdapter;

import java.math.BigInteger;

import io.realm.Realm;
import jp.wasabeef.recyclerview.animators.FadeInLeftAnimator;
import rx.Subscriber;

import static android.app.Activity.RESULT_OK;

public final class ChatPresenter implements Presenter<ChatActivity> {

    private final int VIDEO_REQUEST_CODE = 1;
    private final int WITHDRAW_REQUEST_CODE = 2;

    private ChatActivity activity;
    private OfflineBalance offlineBalance;
    private MessageAdapter messageAdapter;
    private boolean firstViewAttachment = true;
    private Subscriber<Payment> newPaymentSubscriber;
    private ChatStore chatStore;

    @Override
    public void onViewAttached(final ChatActivity activity) {
        this.activity = activity;
        initToolbar();

        if (firstViewAttachment) {
            firstViewAttachment = false;
            initLongLivingObjects();
        }
        initShortLivingObjects();

        // Refrresh state
        this.messageAdapter.notifyDataSetChanged();
        showBalance();
        scrollToBottom(false);
    }

    private void initLongLivingObjects() {
        this.offlineBalance = new OfflineBalance();

        this.chatStore = new ChatStore();
        this.chatStore.getEmptySetObservable().subscribe(this.noStoredChatMessages);
        this.chatStore.getNewMessageObservable().subscribe(this.newChatMessage);

        this.messageAdapter = new MessageAdapter();
        registerMessageClickedObservable();

        this.activity.getBinding().messagesList.setItemAnimator(new FadeInLeftAnimator());
        BaseApplication.get().getUserManager().getObservable().subscribe(this.currentUserSubscriber);

        this.chatStore.load();
    }

    private void initShortLivingObjects() {
        this.newPaymentSubscriber = generateNewPaymentSubscriber();
        BaseApplication.get().getSocketObservables().getPaymentObservable().subscribe(this.newPaymentSubscriber);
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

    private final Subscriber<ChatMessage> newChatMessage = new Subscriber<ChatMessage>() {
        @Override
        public void onCompleted() {}
        @Override
        public void onError(final Throwable e) {}
        @Override
        public void onNext(final ChatMessage chatMessage) {
            messageAdapter.addMessage(chatMessage);
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
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                chatStore.save(chatMessage);
                scrollToBottom(true);
            }
        }, delay);

    }

    private void displayMessage(final ChatMessage chatMessage) {
        displayMessage(chatMessage, 200);
    }

    private void showBalance() {
        final String balance = offlineBalance.toString();
        if (this.activity != null) {
            this.activity.getBinding().balanceBar.setBalance(balance);
        }
    }

    private final Subscriber<User> currentUserSubscriber = new Subscriber<User>() {
        @Override
        public void onCompleted() {}
        @Override
        public void onError(final Throwable e) {}
        @Override
        public void onNext(final User user) {
            this.unsubscribe();
            offlineBalance.setBalance(user.getBalance());
            showBalance();
        }
    };

    private Subscriber<Payment> generateNewPaymentSubscriber() {
        return new Subscriber<Payment>() {
            @Override
            public void onCompleted() {
            }

            @Override
            public void onError(final Throwable e) {
                LogUtil.e(getClass(), e.toString());
            }

            @Override
            public void onNext(final Payment payment) {
                handleNewPayment(payment);
            }
        };
    }

    private void handleNewPayment(final Payment payment) {
        final String amount = payment.getAmount().toString();
        final String message = String.format(this.activity.getResources().getString(R.string.chat__currency_earned), amount);
        final ChatMessage response = new ChatMessage().makeRemoteMessageWithText(message);
        displayMessage(response, 500);

        offlineBalance.setBalance(payment.getNewBalance());

        if (!offlineBalance.hasWithdraw() && offlineBalance.getNumberOfRewards() == 2) {
            showWithdrawMessage();
        }
        showBalance();
    }


    private void showWithdrawMessage() {
        final ChatMessage message = new ChatMessage().makeRemoteWithdrawMessage();
        displayMessage(message, 1000);
    }



    private void withdrawAmountFromAddress(final BigInteger amount, final String walletAddress) {
        final String message = String.format(this.activity.getResources().getString(R.string.chat__withdraw_to_address), amount.toString(), walletAddress);
        final ChatMessage response = new ChatMessage().makeRemoteMessageWithText(message);
        displayMessage(response, 500);
        offlineBalance.subtract(amount);
        showBalance();
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

    private void promptNewVideo() {
        this.activity.getBinding().buttonAnotherVideo.setVisibility(View.VISIBLE);
        this.activity.getBinding().buttonAnotherVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                activity.getBinding().buttonAnotherVideo.setVisibility(View.INVISIBLE);
                showVideoRequestMessage();
                showAVideo();
            }
        });
    }

    @Override
    public void onViewDetached() {
        this.newPaymentSubscriber.unsubscribe();
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
        //rewardCurrency();
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
        intent.putExtra(WithdrawPresenter.INTENT_BALANCE, offlineBalance.getBalance());
        this.activity.startActivityForResult(intent, WITHDRAW_REQUEST_CODE);
        this.activity.overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
    }
}
