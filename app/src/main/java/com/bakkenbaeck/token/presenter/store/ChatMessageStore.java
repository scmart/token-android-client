package com.bakkenbaeck.token.presenter.store;


import com.bakkenbaeck.token.model.ChatMessage;

import rx.subjects.PublishSubject;

public class ChatMessageStore extends RealmStore<ChatMessage> {

    private final PublishSubject<Void> emptySetObservable = PublishSubject.create();
    private final PublishSubject<ChatMessage> newMessageObservable = PublishSubject.create();

    public void load(final String conversationId) {
        this.loadWhere(ChatMessage.class, "conversationId", conversationId);
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
