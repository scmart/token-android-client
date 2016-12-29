package com.bakkenbaeck.token.presenter.store;


import com.bakkenbaeck.token.model.ChatMessage;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import rx.subjects.PublishSubject;

public class ChatMessageStore {

    private final static PublishSubject<ChatMessage> newMessageObservable = PublishSubject.create();
    private final Realm realm;

    public ChatMessageStore() {
        this.realm = Realm.getDefaultInstance();
    }

    public void load(final String conversationId) {
        this.loadWhere("conversationId", conversationId);
    }

    public ChatMessage save(final ChatMessage chatMessage) {
        this.realm.beginTransaction();
        final ChatMessage storedObject = this.realm.copyToRealm(chatMessage);
        this.realm.commitTransaction();
        broadcastNewChatMessage(chatMessage);
        return storedObject;
    }

    public void setSendState(final ChatMessage storedChatMessage, final @ChatMessage.SendState int newState) {
        this.realm.beginTransaction();
        storedChatMessage.setSendState(newState);
        this.realm.insertOrUpdate(storedChatMessage);
        this.realm.commitTransaction();
    }

    private void loadWhere(final String fieldName, final String value) {
        final RealmQuery<ChatMessage> query = realm.where(ChatMessage.class);
        query.equalTo(fieldName, value);
        runAndHandleQuery(query);
    }

    private void runAndHandleQuery(final RealmQuery<ChatMessage> query) {
        final RealmResults<ChatMessage> chatMessages = query.findAll();
        if (chatMessages.size() == 0) {
            onEmptySetAfterLoad();
            return;
        }

        for (final ChatMessage chatMessage : chatMessages) {
            broadcastNewChatMessage(chatMessage);
        }

        onFinishedLoading();
    }

    public PublishSubject<ChatMessage> getNewMessageObservable() {
        return newMessageObservable;
    }

    private void broadcastNewChatMessage(final ChatMessage newMessage) {
        newMessageObservable.onNext(newMessage);
    }

    private void onEmptySetAfterLoad() {}

    private void onFinishedLoading() {}
}
