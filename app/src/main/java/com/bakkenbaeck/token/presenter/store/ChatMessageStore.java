package com.bakkenbaeck.token.presenter.store;


import com.bakkenbaeck.token.model.ChatMessage;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import rx.subjects.PublishSubject;

public class ChatMessageStore {

    private final Realm realm;
    private final RealmChangeListener<RealmResults<ChatMessage>> newMessageListener;
    private final PublishSubject<Void> emptySetObservable = PublishSubject.create();
    private final PublishSubject<ChatMessage> newMessageObservable = PublishSubject.create();
    private RealmResults<ChatMessage> chatMessages;

    public ChatMessageStore() {
        this.realm = Realm.getDefaultInstance();
        this.newMessageListener = new RealmChangeListener<RealmResults<ChatMessage>>() {
            @Override
            public void onChange(final RealmResults<ChatMessage> newMessages) {
                // Just broadcast the last message -- this might be too naive
                onNewChatMessage(newMessages.last());
            }
        };
    }

    public void load(final String conversationId) {
        this.loadWhere("conversationId", conversationId);
    }

    public void save(final ChatMessage chatMessage) {
        this.realm.beginTransaction();
        final ChatMessage storedObject = realm.copyToRealm(chatMessage);
        this.realm.commitTransaction();
        onNewChatMessage(storedObject);
    }

    private void loadWhere(final String fieldName, final String value) {
        final RealmQuery<ChatMessage> query = realm.where(ChatMessage.class);
        query.equalTo(fieldName, value);
        runAndHandleQuery(query);
    }

    private void runAndHandleQuery(final RealmQuery<ChatMessage> query) {
        this.chatMessages = query.findAll();
        this.chatMessages.addChangeListener(this.newMessageListener);
        if (this.chatMessages.size() == 0) {
            onEmptySetAfterLoad();
            return;
        }

        for (final ChatMessage chatMessage : this.chatMessages) {
            onNewChatMessage(chatMessage);
        }

        onFinishedLoading();
    }

    public PublishSubject<Void> getEmptySetObservable() {
        return this.emptySetObservable;
    }

    public PublishSubject<ChatMessage> getNewMessageObservable() {
        return this.newMessageObservable;
    }

    private void onNewChatMessage(final ChatMessage newMessage) {
        // Only broadcast remote messages -- otherwise listening classes will also
        // be informed of messages they have already saved
        if (newMessage.getType() != ChatMessage.TYPE_REMOTE_TEXT) {
            return;
        }

        this.newMessageObservable.onNext(newMessage);
    }

    private void onEmptySetAfterLoad() {
        this.emptySetObservable.onCompleted();
    }

    private void onFinishedLoading() {}


    public void destroy() {
        this.chatMessages.removeChangeListener(this.newMessageListener);
    }
}
