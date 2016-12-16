package com.bakkenbaeck.token.presenter;

import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.bakkenbaeck.token.R;
import com.bakkenbaeck.token.model.ActivityResultHolder;
import com.bakkenbaeck.token.model.ChatMessage;
import com.bakkenbaeck.token.network.ws.model.Message;
import com.bakkenbaeck.token.presenter.store.ChatMessageStore;
import com.bakkenbaeck.token.util.LogUtil;
import com.bakkenbaeck.token.util.OnCompletedObserver;
import com.bakkenbaeck.token.util.OnNextObserver;
import com.bakkenbaeck.token.util.SharedPrefsUtil;
import com.bakkenbaeck.token.view.fragment.QrFragment;
import com.bakkenbaeck.token.view.activity.ChatActivity;
import com.bakkenbaeck.token.view.activity.WithdrawActivity;
import com.bakkenbaeck.token.view.adapter.MessageAdapter;

public final class ChatPresenter implements
        Presenter<ChatActivity>,
        QrFragment.OnFragmentClosed {

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

        // Refresh state
        unpauseMessageAdapter();
        this.messageAdapter.notifyDataSetChanged();
        refreshAnotherOneButtonState();
        scrollToBottom(false);

        initView();
    }

    private void initView() {
        this.activity.getBinding().messagesList.setAdapter(this.messageAdapter);
        this.activity.getBinding().balanceBar.setOnBalanceClicked(new View.OnClickListener() {

            @Override
            public void onClick(final View v) {
                showQrFragment();
            }
        });

        reEnableDialogListeners();
    }

    private void reEnableDialogListeners() {
        QrFragment durationDialog = (QrFragment) this.activity.getSupportFragmentManager().findFragmentByTag(QrFragment.TAG);
        if(durationDialog != null) {
            durationDialog.setOnFragmentClosed(this);
        }
    }
    
    public boolean isAttached() {
        return this.activity != null;
    }

    private void initLongLivingObjects() {
        this.chatMessageStore = new ChatMessageStore();
        this.chatMessageStore.getEmptySetObservable().subscribe(this.noStoredChatMessages);
        this.chatMessageStore.getNewMessageObservable().subscribe(this.newChatMessage);

        this.messageAdapter = new MessageAdapter(activity);

        this.chatMessageStore.load();
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

    private void showWelcomeMessage() {
        final ChatMessage videoMessage = new ChatMessage().makeRemoteVideoMessage(this.activity.getResources().getString(R.string.chat__welcome_message));
        displayMessage(videoMessage, 500);
    }

    private void showVideoRequestMessage() {
        final ChatMessage message = new ChatMessage().makeLocalMessageWithText(this.activity.getResources().getString(R.string.chat__request_video_message));
        displayMessage(message);
    }

    private void displayMessage(final ChatMessage chatMessage, final int delay) {
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                if (SharedPrefsUtil.hasDayChanged()) {
                    final ChatMessage dayMessage = new ChatMessage().makeDayHeader();
                    chatMessageStore.save(dayMessage);
                }

                chatMessageStore.save(chatMessage);
                scrollToBottom(true);
            }
        }, delay);

    }

    private void displayMessage(final ChatMessage chatMessage) {
        displayMessage(chatMessage, 0);
    }

    // Todo - use this
    private final OnNextObserver<Message> newMessageSubscriber = new OnNextObserver<Message>() {
        @Override
        public void onNext(final Message message) {
            if (message.getType() != null && message.getType().equals(ChatMessage.REWARD_EARNED_TYPE)) {
                displayMessage(new ChatMessage().makeRemoteRewardMessage(message), 0);
            } else if (message.shouldShowVideo()) {
                displayMessage(new ChatMessage().makeRemoteVideoMessage(message.toString()), 0);
            } else {
                if (message.getType() != null && message.getType().equals(ChatMessage.DAILY_LIMIT_REACHED)) {
                    promptNewVideo();
                }
                displayMessage(new ChatMessage().makeRemoteMessageWithText(message.toString()), 0);
            }
        }
    };

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
        }, 0);
    }

    private void refreshAnotherOneButtonState() {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if (activity == null) {
                    LogUtil.print(getClass(), "Attempt to refreshAnotherOneButtonState but activity is null");
                    return;
                }

                activity.getBinding().buttonAnotherVideo.setVisibility(
                        isShowingAnotherOneButton
                                ? View.VISIBLE
                                : View.INVISIBLE
                );
            }
        });
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

    @Override
    public void onViewDestroyed() {
        if(messageAdapter != null) {
            messageAdapter.clean();
            messageAdapter = null;
        }
        this.activity = null;
    }

    public void handleActivityResult(final ActivityResultHolder activityResultHolder) {

    }

    public void handleWithdrawClicked() {
        final Intent intent = new Intent(this.activity, WithdrawActivity.class);
        this.activity.startActivityForResult(intent, WITHDRAW_REQUEST_CODE);
        this.activity.overridePendingTransition(R.anim.enter_fade_in, R.anim.exit_fade_out);
    }

    private void showQrFragment() {
        FragmentManager fm = this.activity.getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.setCustomAnimations(R.anim.enter_fade_in, R.anim.exit_fade_out);
        QrFragment qrFragment = QrFragment.newInstance();
        ft.add(R.id.root, qrFragment, QrFragment.TAG).addToBackStack(QrFragment.TAG).commit();
        qrFragment.setOnFragmentClosed(this);
    }

    public void removeQrFragment() {
        FragmentManager fm = this.activity.getSupportFragmentManager();
        Fragment qrFragment = fm.findFragmentByTag(QrFragment.TAG);

        if(qrFragment != null) {
            FragmentTransaction ft = fm.beginTransaction();
            ft.setCustomAnimations(R.anim.enter_fade_in, R.anim.exit_fade_out);
            ft.remove(qrFragment).commit();
            fm.popBackStackImmediate(QrFragment.TAG,  FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }
    }

    @Override
    public void onClose() {
        removeQrFragment();
    }
}
