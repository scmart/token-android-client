package com.bakkenbaeck.token.presenter.store;


import com.bakkenbaeck.token.model.ChatMessage;

import rx.subjects.PublishSubject;

public class ChatMessageStore extends RealmStore<ChatMessage> {

    private final PublishSubject<Void> emptySetObservable = PublishSubject.create();
    private final PublishSubject<ChatMessage> newMessageObservable = PublishSubject.create();
    private String conversationId;

    public void load(final String conversationId) {
        this.conversationId = conversationId;
        this.loadWhere(ChatMessage.class, "conversationId", this.conversationId);
    }

    @Override
    public void save(final ChatMessage chatMessage) {
        chatMessage.setConversationId(this.conversationId);
        super.save(chatMessage);
    }

    public PublishSubject<Void> getEmptySetObservable() {
        return this.emptySetObservable;
    }

    public PublishSubject<ChatMessage> getNewMessageObservable() {
        return this.newMessageObservable;
    }

    @Override
    void onNewObject(final ChatMessage newMessage) {
        this.newMessageObservable.onNext(newMessage);
    }

    @Override
    void onEmptySetAfterLoad() {
        this.emptySetObservable.onCompleted();
    }

    @Override
    void onFinishedLoading() {}
}
