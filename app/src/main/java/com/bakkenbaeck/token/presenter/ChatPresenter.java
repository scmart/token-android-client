package com.bakkenbaeck.token.presenter;

import android.os.Build;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.PathInterpolator;
import android.widget.Toast;

import com.bakkenbaeck.token.model.local.ChatMessage;
import com.bakkenbaeck.token.model.local.User;
import com.bakkenbaeck.token.presenter.store.ChatMessageStore;
import com.bakkenbaeck.token.util.OnNextSubscriber;
import com.bakkenbaeck.token.util.SingleSuccessSubscriber;
import com.bakkenbaeck.token.view.Animation.SlideUpAnimator;
import com.bakkenbaeck.token.view.BaseApplication;
import com.bakkenbaeck.token.view.activity.ChatActivity;
import com.bakkenbaeck.token.view.adapter.MessageAdapter;
import com.bakkenbaeck.token.view.custom.SpeedyLinearLayoutManager;

import io.realm.RealmResults;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public final class ChatPresenter implements
        Presenter<ChatActivity> {

    private ChatActivity activity;
    private MessageAdapter messageAdapter;
    private boolean firstViewAttachment = true;
    private ChatMessageStore chatMessageStore;
    private User remoteUser;
    private SpeedyLinearLayoutManager layoutManager;

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
        this.chatMessageStore = new ChatMessageStore();
        this.chatMessageStore.getNewMessageObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this.handleNewMessage);
        this.chatMessageStore.getUpdatedMessageObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this.handleUpdatedMessage);
        this.chatMessageStore.load(this.remoteUser.getAddress())
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

    private void initButtons() {
        this.activity.getBinding().sendButton.setOnClickListener(this.sendButtonClicked);
    }

    private final View.OnClickListener sendButtonClicked = new View.OnClickListener() {
        @Override
        public void onClick(final View v) {
            if (userInputInvalid()) {
                return;
            }

            final String userInput = activity.getBinding().userInput.getText().toString();
            activity.getBinding().userInput.setText(null);

            final ChatMessage message = new ChatMessage().makeLocalMessage(remoteUser.getAddress(), userInput);
            BaseApplication.get()
                    .getTokenManager()
                    .getSignalManager()
                    .sendMessage(message);
        }

        private boolean userInputInvalid() {
            return activity.getBinding().userInput.getText().toString().trim().length() == 0;
        }
    };

    private final OnNextSubscriber<ChatMessage> handleNewMessage = new OnNextSubscriber<ChatMessage>() {
        @Override
        public void onNext(final ChatMessage chatMessage) {
            messageAdapter.addMessage(chatMessage);
            updateEmptyState();
            tryScrollToBottom(true);
        }
    };

    private final OnNextSubscriber<ChatMessage> handleUpdatedMessage = new OnNextSubscriber<ChatMessage>() {
        @Override
        public void onNext(final ChatMessage chatMessage) {
            if (chatMessage.getSendState() != ChatMessage.STATE_FAILED) {
                return;
            }

            Toast.makeText(activity, "Failed to send: " + chatMessage.getText(), Toast.LENGTH_SHORT).show();
        }
    };

    private final SingleSuccessSubscriber<RealmResults<ChatMessage>> handleLoadMessages = new SingleSuccessSubscriber<RealmResults<ChatMessage>>() {
        @Override
        public void onSuccess(final RealmResults<ChatMessage> chatMessages) {
            if (chatMessages.size() > 0) {
                messageAdapter.addMessages(chatMessages);
                forceScrollToBottom();
                updateEmptyState();
            }

            this.unsubscribe();
        }
    };

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
}
