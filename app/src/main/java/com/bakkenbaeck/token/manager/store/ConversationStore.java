package com.bakkenbaeck.token.presenter.store;


import android.util.Pair;

import com.bakkenbaeck.token.model.local.Conversation;
import com.bakkenbaeck.token.model.local.SofaMessage;
import com.bakkenbaeck.token.model.local.User;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import io.realm.Sort;
import rx.Single;
import rx.subjects.PublishSubject;

public class ConversationStore {
    private final Realm realm;
    private static String watchedConversationId;
    private final static PublishSubject<SofaMessage> newMessageObservable = PublishSubject.create();
    private final static PublishSubject<SofaMessage> updatedMessageObservable = PublishSubject.create();
    private final static PublishSubject<Conversation> conversationChangedObservable = PublishSubject.create();

    public ConversationStore() {
        this.realm = Realm.getDefaultInstance();
    }

    // Returns a pair of RxSubjects, the first being the observable for new messages
    // the second being the observable for updated messages.
    public Pair<PublishSubject<SofaMessage>, PublishSubject<SofaMessage>> registerForChanges(final String conversationId) {
        watchedConversationId = conversationId;
        resetUnreadMessageCounter(conversationId);
        return new Pair<>(newMessageObservable, updatedMessageObservable);
    }

    public void stopListeningForChanges() {
        watchedConversationId = null;
    }

    public PublishSubject<Conversation> getConversationChangedObservable() {
        return conversationChangedObservable;
    }

    public void saveNewMessage(final User user, final SofaMessage message) {
        realm.beginTransaction();
        final Conversation existingConversation = loadWhere("conversationId", user.getOwnerAddress());
        final Conversation conversationToStore = existingConversation == null
                ? new Conversation(user)
                : existingConversation;
        final SofaMessage storedMessage = realm.copyToRealm(message);
        conversationToStore.setLatestMessage(storedMessage);
        conversationToStore.setNumberOfUnread(calculateNumberOfUnread(conversationToStore));
        final Conversation storedConversation = realm.copyToRealmOrUpdate(conversationToStore);
        realm.commitTransaction();
        broadcastNewChatMessage(user.getOwnerAddress(), message);
        broadcastConversationChanged(realm.copyFromRealm(storedConversation));
    }

    private int calculateNumberOfUnread(final Conversation conversationToStore) {
        // If we are watching the current conversation the message is automatically read.
        if (conversationToStore.getMember().getOwnerAddress().equals(watchedConversationId)) {
            return 0;
        }
        final int currentNumberOfUnread = conversationToStore.getNumberOfUnread();
        return currentNumberOfUnread + 1;
    }

    private void resetUnreadMessageCounter(final String conversationId) {
        final Conversation storedConversation = loadWhere("conversationId", conversationId);
        if (storedConversation == null) {
            return;
        }

        realm.beginTransaction();
        storedConversation.setNumberOfUnread(0);
        realm.insertOrUpdate(storedConversation);
        realm.commitTransaction();
        broadcastConversationChanged(realm.copyFromRealm(storedConversation));
    }

    public List<Conversation> loadAll() {
        final RealmQuery<Conversation> query = realm.where(Conversation.class);
        final RealmResults<Conversation> results = query.findAllSorted("updatedTime", Sort.DESCENDING);
        return realm.copyFromRealm(results);
    }

    private void broadcastConversationChanged(final Conversation conversation) {
        conversationChangedObservable.onNext(conversation);
    }

    public Single<Conversation> loadByAddress(final String address) {
        return Single.fromCallable(() -> loadWhere("conversationId", address));
    }

    private Conversation loadWhere(final String fieldName, final String value) {
        final RealmQuery<Conversation> query = realm.where(Conversation.class);
        query.equalTo(fieldName, value);
        return query.findFirst();
    }

    public void updateMessage(final User user, final SofaMessage message) {
        realm.beginTransaction();
        realm.insertOrUpdate(message);
        realm.commitTransaction();
        broadcastUpdatedChatMessage(user.getOwnerAddress(), message);
    }

    private void broadcastNewChatMessage(final String conversationId, final SofaMessage newMessage) {
        if (watchedConversationId == null || !watchedConversationId.equals(conversationId)) {
            return;
        }
        newMessageObservable.onNext(newMessage);
    }

    private void broadcastUpdatedChatMessage(final String conversationId, final SofaMessage updatedMessage) {
        if (watchedConversationId == null || !watchedConversationId.equals(conversationId)) {
            return;
        }
        updatedMessageObservable.onNext(updatedMessage);
    }
}
