/*
 * 	Copyright (c) 2017. Token Browser, Inc
 *
 * 	This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
