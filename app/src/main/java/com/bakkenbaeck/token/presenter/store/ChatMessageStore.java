package com.bakkenbaeck.token.presenter.store;


import com.bakkenbaeck.token.model.ChatMessage;

import java.util.concurrent.Callable;

import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import rx.Single;
import rx.subjects.PublishSubject;

public class ChatMessageStore {

    private final static PublishSubject<ChatMessage> newMessageObservable = PublishSubject.create();
    private final static PublishSubject<ChatMessage> updatedMessageObservable = PublishSubject.create();
    private final Realm realm;

    public ChatMessageStore() {
        this.realm = Realm.getDefaultInstance();
    }

    public Single<RealmResults<ChatMessage>> load(final String conversationId) {
        return Single.fromCallable(new Callable<RealmResults<ChatMessage>>() {
            @Override
            public RealmResults<ChatMessage> call() throws Exception {
                return loadWhere("conversationId", conversationId);
            }
        });
    }

    public void save(final ChatMessage chatMessage) {
        this.realm.beginTransaction();
        this.realm.insert(chatMessage);
        this.realm.commitTransaction();
        broadcastNewChatMessage(chatMessage);
    }

    public void update(final ChatMessage chatMessage) {
        this.realm.beginTransaction();
        this.realm.insertOrUpdate(chatMessage);
        this.realm.commitTransaction();
        broadcastUpdatedChatMessage(chatMessage);
    }

    private RealmResults<ChatMessage> loadWhere(final String fieldName, final String value) {
        final RealmQuery<ChatMessage> query = realm.where(ChatMessage.class);
        query.equalTo(fieldName, value);
        return query.findAll();
    }

    public PublishSubject<ChatMessage> getNewMessageObservable() {
        return newMessageObservable;
    }

    public PublishSubject<ChatMessage> getUpdatedMessageObservable() {
        return updatedMessageObservable;
    }

    private void broadcastNewChatMessage(final ChatMessage newMessage) {
        newMessageObservable.onNext(newMessage);
    }

    private void broadcastUpdatedChatMessage(final ChatMessage updatedMessage) {
        updatedMessageObservable.onNext(updatedMessage);
    }
}
