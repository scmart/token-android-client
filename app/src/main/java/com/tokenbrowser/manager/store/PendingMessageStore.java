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

    public List<PendingMessage> getAllPendingMessages() {
        final Realm realm = Realm.getDefaultInstance();
        final RealmResults<PendingMessage> result = realm
                .where(PendingMessage.class)
                .findAll();
        final List<PendingMessage> pendingMessages = realm.copyFromRealm(result);
        realm.close();
        return pendingMessages;
    }

    public void delete(final SofaMessage sofaMessage) {
        final Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        final PendingMessage result = realm
                .where(PendingMessage.class)
                .equalTo("privateKey", sofaMessage.getPrivateKey())
                .findFirst();

        if (result!= null) {
            result.deleteFromRealm();
        }

        realm.commitTransaction();
        realm.close();
    }
}
