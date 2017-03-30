package com.tokenbrowser.manager.store;


import com.tokenbrowser.model.local.PendingMessage;
import com.tokenbrowser.model.local.SofaMessage;
import com.tokenbrowser.model.local.User;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;

public class PendingMessageStore {

    public void save(final User receiver, final SofaMessage message) {
        final PendingMessage pendingMessage = new PendingMessage()
                .setReceiver(receiver)
                .setSofaMessage(message);

        final Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        realm.insertOrUpdate(pendingMessage);
        realm.commitTransaction();
        realm.close();
    }

    // Gets, and removes all messages. After calling this any pending messages will be removed
    public List<PendingMessage> fetchAllPendingMessages() {
        final Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        final RealmResults<PendingMessage> result = realm
                .where(PendingMessage.class)
                .findAll();
        final List<PendingMessage> pendingMessages = realm.copyFromRealm(result);
        result.deleteAllFromRealm();
        realm.commitTransaction();
        realm.close();
        return pendingMessages;
    }
}
