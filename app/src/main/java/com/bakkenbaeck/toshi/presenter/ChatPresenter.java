package com.bakkenbaeck.toshi.presenter;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.bakkenbaeck.toshi.R;
import com.bakkenbaeck.toshi.model.ActivityResultHolder;
import com.bakkenbaeck.toshi.model.ChatMessage;
import com.bakkenbaeck.toshi.model.OfflineBalance;
import com.bakkenbaeck.toshi.view.activity.ChatActivity;
import com.bakkenbaeck.toshi.view.activity.VideoActivity;
import com.bakkenbaeck.toshi.view.activity.WithdrawActivity;
import com.bakkenbaeck.toshi.view.adapter.MessageAdapter;

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

    @Override
    public void onViewAttached(final ChatActivity activity) {
        this.activity = activity;

        if (firstViewAttachment) {
            firstViewAttachment = false;
            this.offlineBalance = new OfflineBalance();
            this.messageAdapter = new MessageAdapter(this.adapterListener);
            registerObservable();
            this.activity.getBinding().messagesList.setItemAnimator(new FadeInLeftAnimator());
        }

        this.activity.getBinding().messagesList.setAdapter(this.messageAdapter);
        this.messageAdapter.notifyDataSetChanged();

        initToolbar();
        showBalance();
        scrollToBottom();
    }

    private void initToolbar() {
        final String title = this.activity.getResources().getString(R.string.chat__title);
        final Toolbar toolbar = this.activity.getBinding().toolbar;
        this.activity.setSupportActionBar(toolbar);
        this.activity.getSupportActionBar().setTitle(title);
    }

    private final MessageAdapter.AdapterListener adapterListener = new MessageAdapter.AdapterListener() {
        @Override
        public void onNoStoredMessages() {
            showWelcomeMessage();
        }

        @Override
        public void onMessagesLoaded(final boolean hasUnwatchedVideo) {
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

    private void rewardCurrency() {
        final double reward = offlineBalance.addRandomAmount();
        final String message = String.format(this.activity.getResources().getString(R.string.chat__currency_earned), reward);
        final ChatMessage response = new ChatMessage().makeRemoteMessageWithText(message);
        displayMessage(response, 500);

        if (!offlineBalance.hasWithdraw() && offlineBalance.getNumberOfRewards() == 2) {
            showWithdrawMessage();
        }
        showBalance();
    }

    private void showWithdrawMessage() {
        final ChatMessage message = new ChatMessage().makeRemoteWithdrawMessage();
        displayMessage(message, 1000);
    }

    private void displayMessage(final ChatMessage chatMessage, final int delay) {
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                messageAdapter.addMessage(chatMessage);
                scrollToBottom();
            }
        }, delay);

    }

    private void displayMessage(final ChatMessage chatMessage) {
        displayMessage(chatMessage, 200);
    }

    private void showBalance() {
        final String balance = String.format(this.activity.getResources().getString(R.string.balance__current_balance), offlineBalance.getBalance());
        if (this.activity != null) {
            this.activity.getBinding().balanceBar.setBalance(balance);
        }
    }

    private void withdrawAmountFromAddress(final double amount, final String walletAddress) {
        final String message = String.format(this.activity.getResources().getString(R.string.chat__withdraw_to_address), amount, walletAddress);
        final ChatMessage response = new ChatMessage().makeRemoteMessageWithText(message);
        displayMessage(response, 500);
        offlineBalance.subtract(amount);
        showBalance();
    }

    private void scrollToBottom() {
        if (this.activity != null && this.messageAdapter.getItemCount() > 0) {
            this.activity.getBinding().messagesList.smoothScrollToPosition(this.messageAdapter.getItemCount() -1);
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
        this.activity = null;
    }

    private void registerObservable() {
        this.messageAdapter.getPositionClicks().subscribe(this.clicksSubscriber);
    }

    private void unregisterObservable() {
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
        unregisterObservable();
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
            final double amount = activityResultHolder.getIntent().getDoubleExtra(WithdrawPresenter.INTENT_WITHDRAW_AMOUNT, 0);
            withdrawAmountFromAddress(amount, address);
        }
    }

    private void handleVideoCompleted(final ActivityResultHolder activityResultHolder) {
        markVideoAsWatched(activityResultHolder);
        rewardCurrency();
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
