package com.bakkenbaeck.token.presenter.store;


import com.bakkenbaeck.token.model.ChatMessage;

import java.util.concurrent.Callable;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import rx.Single;
import rx.subjects.PublishSubject;

public class ChatMessageStore {

    private final static PublishSubject<ChatMessage> newMessageObservable = PublishSubject.create();
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

    private RealmResults<ChatMessage> loadWhere(final String fieldName, final String value) {
        final RealmQuery<ChatMessage> query = realm.where(ChatMessage.class);
        query.equalTo(fieldName, value);
        return query.findAll();
    }

    public PublishSubject<ChatMessage> getNewMessageObservable() {
        return newMessageObservable;
    }

    private void broadcastNewChatMessage(final ChatMessage newMessage) {
        newMessageObservable.onNext(newMessage);
    }
}
