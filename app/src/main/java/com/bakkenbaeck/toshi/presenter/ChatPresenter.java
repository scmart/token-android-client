package com.bakkenbaeck.toshi.presenter;

import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.View;

import com.bakkenbaeck.toshi.R;
import com.bakkenbaeck.toshi.model.ActivityResultHolder;
import com.bakkenbaeck.toshi.model.ChatMessage;
import com.bakkenbaeck.toshi.model.LocalBalance;
import com.bakkenbaeck.toshi.network.ws.model.ConnectionState;
import com.bakkenbaeck.toshi.network.ws.model.Message;
import com.bakkenbaeck.toshi.presenter.store.ChatMessageStore;
import com.bakkenbaeck.toshi.util.OnCompletedObserver;
import com.bakkenbaeck.toshi.util.OnNextObserver;
import com.bakkenbaeck.toshi.util.OnNextSubscriber;
import com.bakkenbaeck.toshi.util.OnSingleClickListener;
import com.bakkenbaeck.toshi.view.BaseApplication;
import com.bakkenbaeck.toshi.view.activity.ChatActivity;
import com.bakkenbaeck.toshi.view.activity.VideoActivity;
import com.bakkenbaeck.toshi.view.activity.WithdrawActivity;
import com.bakkenbaeck.toshi.view.adapter.MessageAdapter;

import java.math.BigDecimal;

import io.realm.Realm;

import static android.app.Activity.RESULT_OK;

public final class ChatPresenter implements Presenter<ChatActivity> {
    private static final String TAG = "ChatPresenter";
    private final int VIDEO_REQUEST_CODE = 1;
    private final int WITHDRAW_REQUEST_CODE = 2;

    private ChatActivity activity;
    private MessageAdapter messageAdapter;
    private boolean firstViewAttachment = true;
    private boolean isShowingAnotherOneButton;
    private ChatMessageStore chatMessageStore;
    private Snackbar networkStateSnackbar;

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
        unpauseMessageAdapter();
        this.messageAdapter.notifyDataSetChanged();
        refreshAnotherOneButtonState();
        scrollToBottom(false);
    }

    public boolean isAttached() {
        return this.activity != null;
    }

    private void initLongLivingObjects() {
        initNetworkStateSnackbar();

        this.chatMessageStore = new ChatMessageStore();
        this.chatMessageStore.getEmptySetObservable().subscribe(this.noStoredChatMessages);
        this.chatMessageStore.getNewMessageObservable().subscribe(this.newChatMessage);
        this.chatMessageStore.getUnwatchedVideoObservable().subscribe(this.unwatchedVideo);
        BaseApplication.get().getSocketObservables().getMessageObservable().subscribe(this.newMessageSubscriber);
        BaseApplication.get().getSocketObservables().getConnectionObservable().subscribe(this.connectionStateSubscriber);

        this.messageAdapter = new MessageAdapter();
        registerMessageClickedObservable();

        this.chatMessageStore.load();
    }

    private void initNetworkStateSnackbar() {
        this.networkStateSnackbar = Snackbar.make(
                this.activity.getBinding().balanceBar,
                Html.fromHtml(this.activity.getString(R.string.socket__connecting_state)),
                Snackbar.LENGTH_INDEFINITE);
    }

    private void initShortLivingObjects() {
        this.activity.getBinding().messagesList.setAdapter(this.messageAdapter);
        BaseApplication.get().getLocalBalanceManager().getObservable().subscribe(this.newBalanceSubscriber);
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
        showAVideo(response);
    }

    private void showAVideo(ChatMessage message) {
        ChatMessage video;
        if(message == null){
           video = new ChatMessage().makeRemoteVideoMessage("");
        }else{
            video = new ChatMessage().makeRemoteVideoMessage(message.getText());
        }

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

    private final OnNextObserver<LocalBalance> newBalanceSubscriber = new OnNextObserver<LocalBalance>() {
        @Override
        public void onNext(final LocalBalance newBalance) {
            if (activity != null && newBalance != null) {
                activity.getBinding().balanceBar.setBalance(newBalance.unconfirmedBalanceString());
            }
        }
    };

    private final OnNextObserver<Message> newMessageSubscriber = new OnNextObserver<Message>() {
        @Override
        public void onNext(final Message message) {
            final ChatMessage response = new ChatMessage().makeRemoteRewardMessage(message);
            displayMessage(response, 500);
        }
    };

    private void withdrawAmountFromAddress(final BigDecimal amount, final String walletAddress) {
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

    private void unpauseMessageAdapter() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                messageAdapter.unPauseRendering();
                scrollToBottom(true);
            }
        }, 500);
    }

    private void refreshAnotherOneButtonState() {
        this.activity.getBinding().buttonAnotherVideo.setVisibility(
                this.isShowingAnotherOneButton
                        ? View.VISIBLE
                        : View.INVISIBLE
        );

        if (this.isShowingAnotherOneButton) {
            this.activity.getBinding().buttonAnotherVideo.setOnClickListener(new OnSingleClickListener() {
                @Override
                public void onSingleClick(final View view) {
                    isShowingAnotherOneButton = false;
                    refreshAnotherOneButtonState();
                    showVideoRequestMessage();
                    showAVideo(null);
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
        this.messageAdapter.pauseRendering();
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

    private final OnNextSubscriber<Integer> clicksSubscriber = new OnNextSubscriber<Integer>() {
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
            final BigDecimal amount = (BigDecimal) activityResultHolder.getIntent().getSerializableExtra(WithdrawPresenter.INTENT_WITHDRAW_AMOUNT);
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
        this.activity.overridePendingTransition(R.anim.enter_fade_in, R.anim.exit_fade_out);
    }

    private final OnNextObserver<ConnectionState> connectionStateSubscriber = new OnNextObserver<ConnectionState>() {
        @Override
        public void onNext(final ConnectionState connectionState) {
            if (connectionState == ConnectionState.CONNECTING) {
                if (networkStateSnackbar.isShownOrQueued()) {
                    return;
                }
                networkStateSnackbar.show();
            } else {
                networkStateSnackbar.dismiss();
            }
        }
    };
}
