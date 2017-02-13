package com.bakkenbaeck.token.presenter.store;


import com.bakkenbaeck.token.model.local.ChatMessage;
import com.bakkenbaeck.token.model.local.Conversation;
import com.bakkenbaeck.token.model.local.User;
import com.bakkenbaeck.token.util.SingleSuccessSubscriber;

import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import rx.Single;
import rx.subjects.PublishSubject;

public class ConversationStore {

    private final static PublishSubject<ChatMessage> newMessageObservable = PublishSubject.create();
    private final static PublishSubject<ChatMessage> updatedMessageObservable = PublishSubject.create();

    private final Realm realm;

    public ConversationStore() {
        this.realm = Realm.getDefaultInstance();
    }

    public PublishSubject<ChatMessage> getNewMessageObservable() {
        return newMessageObservable;
    }

    public PublishSubject<ChatMessage> getUpdatedMessageObservable() {
        return updatedMessageObservable;
    }

    public void saveNewMessage(final User user, final ChatMessage message) {

        loadByAddress(user.getOwnerAddress())
                .subscribe(new SingleSuccessSubscriber<Conversation>() {
                    @Override
                    public void onSuccess(final Conversation conversation) {
                        realm.beginTransaction();

                        final Conversation conversationToStore = conversation == null
                                ? new Conversation(user)
                                : conversation;
                        final ChatMessage storedMessage = realm.copyToRealm(message);
                        conversationToStore.setLatestMessage(storedMessage);
                        realm.copyToRealmOrUpdate(conversationToStore);
                        realm.commitTransaction();
                        broadcastNewChatMessage(message);
                    }
                });
    }

    public Single<RealmResults<Conversation>> loadAll() {
        return Single.fromCallable(() -> {
            final RealmQuery<Conversation> query = realm.where(Conversation.class);
            return query.findAll();
        });
    }

    public Single<Conversation> loadByAddress(final String address) {
        return Single.fromCallable(() -> loadWhere("conversationId", address));
    }

    private Conversation loadWhere(final String fieldName, final String value) {
        final RealmQuery<Conversation> query = realm.where(Conversation.class);
        query.equalTo(fieldName, value);
        return query.findFirst();
    }

    public void updateMessage(final ChatMessage message) {
        realm.beginTransaction();
        realm.insertOrUpdate(message);
        realm.commitTransaction();
        broadcastUpdatedChatMessage(message);
    }

    private void broadcastNewChatMessage(final ChatMessage newMessage) {
        newMessageObservable.onNext(newMessage);
    }

    private void broadcastUpdatedChatMessage(final ChatMessage updatedMessage) {
        updatedMessageObservable.onNext(updatedMessage);
    }
}
