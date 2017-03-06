package com.tokenbrowser.manager.store;


import android.util.Pair;

import com.tokenbrowser.model.local.Conversation;
import com.tokenbrowser.model.local.SofaMessage;
import com.tokenbrowser.model.local.User;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import io.realm.Sort;
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
        final Conversation existingConversation = loadWhere("conversationId", user.getTokenId());
        final Conversation conversationToStore = existingConversation == null
                ? new Conversation(user)
                : existingConversation;
        final SofaMessage storedMessage = realm.copyToRealmOrUpdate(message);
        conversationToStore.setLatestMessage(storedMessage);
        conversationToStore.setNumberOfUnread(calculateNumberOfUnread(conversationToStore));
        final Conversation storedConversation = realm.copyToRealmOrUpdate(conversationToStore);
        realm.commitTransaction();
        broadcastNewChatMessage(user.getTokenId(), message);
        broadcastConversationChanged(realm.copyFromRealm(storedConversation));
    }

    private int calculateNumberOfUnread(final Conversation conversationToStore) {
        // If we are watching the current conversation the message is automatically read.
        if (conversationToStore.getMember().getTokenId().equals(watchedConversationId)) {
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
        broadcastConversationChanged(storedConversation);
    }

    public List<Conversation> loadAll() {
        final RealmQuery<Conversation> query = realm.where(Conversation.class);
        final RealmResults<Conversation> results = query.findAllSorted("updatedTime", Sort.DESCENDING);
        return realm.copyFromRealm(results);
    }

    private void broadcastConversationChanged(final Conversation conversation) {
        conversationChangedObservable.onNext(conversation);
    }

    public Conversation loadByAddress(final String address) {
        resetUnreadMessageCounter(address);
        return loadWhere("conversationId", address);
    }

    private Conversation loadWhere(final String fieldName, final String value) {
        final Conversation result = realm
                .where(Conversation.class)
                .equalTo(fieldName, value)
                .findFirst();
        if (result == null) {
            return null;
        }
        return realm.copyFromRealm(result);
    }

    public void updateMessage(final User user, final SofaMessage message) {
        realm.beginTransaction();
        realm.insertOrUpdate(message);
        realm.commitTransaction();
        broadcastUpdatedChatMessage(user.getTokenId(), message);
    }

    public boolean areUnreadMessages() {
        final Realm localRealmInstance = Realm.getDefaultInstance();
        final Conversation result = localRealmInstance
                .where(Conversation.class)
                .greaterThan("numberOfUnread", 0)
                .findFirst();
        final boolean areUnreadMessages = result != null;
        localRealmInstance.close();
        return areUnreadMessages;
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
