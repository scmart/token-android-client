package com.bakkenbaeck.token.presenter;

import android.os.Handler;
import android.os.Looper;
import android.support.v7.widget.Toolbar;

import com.bakkenbaeck.token.R;
import com.bakkenbaeck.token.model.ChatMessage;
import com.bakkenbaeck.token.model.Contact;
import com.bakkenbaeck.token.network.ws.model.Message;
import com.bakkenbaeck.token.presenter.store.ChatMessageStore;
import com.bakkenbaeck.token.util.OnNextObserver;
import com.bakkenbaeck.token.util.SharedPrefsUtil;
import com.bakkenbaeck.token.view.activity.ChatActivity;
import com.bakkenbaeck.token.view.adapter.MessageAdapter;

public final class ChatPresenter implements
        Presenter<ChatActivity> {

    private ChatActivity activity;
    private MessageAdapter messageAdapter;
    private boolean firstViewAttachment = true;
    private ChatMessageStore chatMessageStore;
    private Contact contact;

    @Override
    public void onViewAttached(final ChatActivity activity) {
        this.activity = activity;
        initToolbar();

        if (firstViewAttachment) {
            firstViewAttachment = false;
            initLongLivingObjects();
        }

        // Refresh state
        unPauseAdapterRendering();
        this.messageAdapter.notifyDataSetChanged();
        scrollToBottom(false);

        initView();
    }

    private void initView() {
        this.activity.getBinding().messagesList.setAdapter(this.messageAdapter);
    }

    private void initLongLivingObjects() {
        this.chatMessageStore = new ChatMessageStore();
        this.chatMessageStore.getNewMessageObservable().subscribe(this.newChatMessage);

        this.messageAdapter = new MessageAdapter(activity);

        this.chatMessageStore.load(this.contact.getConversationId());
    }

    private void initToolbar() {
        final String title = this.activity.getResources().getString(R.string.chat__title);
        final Toolbar toolbar = this.activity.getBinding().toolbar;
        this.activity.setSupportActionBar(toolbar);
        this.activity.getSupportActionBar().setTitle(title);
    }

    private final OnNextObserver<ChatMessage> newChatMessage = new OnNextObserver<ChatMessage>() {
        @Override
        public void onNext(final ChatMessage chatMessage) {
            messageAdapter.addMessage(chatMessage);
        }
    };

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
                displayMessage(new ChatMessage().makeRemoteRewardMessage(message), 0);;
            } else {
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

    private void unPauseAdapterRendering() {
        messageAdapter.unPauseRendering();
        scrollToBottom(true);
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

    public void setPassedInContact(final Contact contact) {
        this.contact = contact;
    }
}
