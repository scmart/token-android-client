package com.bakkenbaeck.token.presenter;

import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.PathInterpolator;
import android.widget.Toast;

import com.bakkenbaeck.token.crypto.signal.model.OutgoingMessage;
import com.bakkenbaeck.token.model.ChatMessage;
import com.bakkenbaeck.token.model.Contact;
import com.bakkenbaeck.token.presenter.store.ChatMessageStore;
import com.bakkenbaeck.token.util.OnNextObserver;
import com.bakkenbaeck.token.util.OnNextSubscriber;
import com.bakkenbaeck.token.view.Animation.SlideUpAnimator;
import com.bakkenbaeck.token.view.BaseApplication;
import com.bakkenbaeck.token.view.activity.ChatActivity;
import com.bakkenbaeck.token.view.adapter.MessageAdapter;
import com.bakkenbaeck.token.view.custom.SpeedyLinearLayoutManager;

import rx.Subscriber;

public final class ChatPresenter implements
        Presenter<ChatActivity> {

    private ChatActivity activity;
    private MessageAdapter messageAdapter;
    private boolean firstViewAttachment = true;
    private ChatMessageStore chatMessageStore;
    private Contact contact;
    private SpeedyLinearLayoutManager layoutManager;

    public void setPassedInContact(final Contact contact) {
        this.contact = contact;
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
        this.activity.getBinding().title.setText(this.contact.getName());
        this.activity.getBinding().avatar.setImageBitmap(this.contact.getImage());
        this.activity.getBinding().backButton.setOnClickListener(this.backButtonClickListener);
    }

    private void initLongLivingObjects() {
        this.chatMessageStore = new ChatMessageStore();
        this.chatMessageStore.getNewMessageObservable().subscribe(this.newChatMessage);
        this.messageAdapter = new MessageAdapter();
        this.chatMessageStore.load(this.contact.getConversationId());
        BaseApplication.get()
                .getTokenManager()
                .getSignalManager()
                .getFailedMessagesObservable()
                .subscribe(this.failedMessagesSubscriber);
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
        forceScrollToBottom();
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


            // Store in Local DB
            final String userInput = activity.getBinding().userInput.getText().toString();
            final ChatMessage message = new ChatMessage().makeLocalMessage(contact.getConversationId(), userInput);
            chatMessageStore.save(message);
            activity.getBinding().userInput.setText(null);

            // Send to backend
            final OutgoingMessage outgoingMessage = new OutgoingMessage()
                    .setAddress(contact.getConversationId())
                    .setBody(userInput)
                    .setId(messageAdapter.getItemCount());
            BaseApplication.get()
                    .getTokenManager()
                    .getSignalManager()
                    .sendMessage(outgoingMessage);
        }

        private boolean userInputInvalid() {
            return activity.getBinding().userInput.getText().toString().trim().length() == 0;
        }
    };

    private final Subscriber<OutgoingMessage> failedMessagesSubscriber = new OnNextSubscriber<OutgoingMessage>() {
        @Override
        public void onNext(final OutgoingMessage message) {
            new Handler(Looper.getMainLooper())
                    .post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(activity, "Unable to send message: " + message.getBody(), Toast.LENGTH_LONG).show();
                        }
                    });
        }
    };

    private final OnNextObserver<ChatMessage> newChatMessage = new OnNextObserver<ChatMessage>() {
        @Override
        public void onNext(final ChatMessage chatMessage) {
            messageAdapter.addMessage(chatMessage);
            tryScrollToBottom(true);
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

    @Override
    public void onViewDetached() {
        this.activity = null;
    }

    @Override
    public void onViewDestroyed() {
        if(messageAdapter != null) {
            messageAdapter = null;
        }
        this.failedMessagesSubscriber.unsubscribe();
        this.activity = null;
    }

    private final View.OnClickListener backButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(final View v) {
            activity.onBackPressed();
        }
    };
}
