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


import com.tokenbrowser.model.local.Contact;
import com.tokenbrowser.model.local.User;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import rx.Single;

public class ContactStore {

    public boolean userIsAContact(final User user) {
        final Realm realm = Realm.getDefaultInstance();
        final boolean result = realm
                .where(Contact.class)
                .equalTo("owner_address", user.getTokenId())
                .findFirst() != null;
        realm.close();
        return result;
    }

    public void save(final User user) {
        final Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        final User storedUser = realm.copyToRealmOrUpdate(user);
        final Contact contact = new Contact(storedUser);
        realm.insert(contact);
        realm.commitTransaction();
        realm.close();
    }

    public void delete(final User user) {
        final Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        realm
                .where(Contact.class)
                .equalTo("owner_address", user.getTokenId())
                .findFirst()
                .deleteFromRealm();
        realm.commitTransaction();
        realm.close();
    }

    public Single<List<Contact>> loadAll() {
        final Realm realm = Realm.getDefaultInstance();
        final RealmQuery<Contact> query = realm.where(Contact.class);
        final RealmResults<Contact> results = query.findAll();
        final List<Contact> retVal = realm.copyFromRealm(results.sort("user.name"));
        realm.close();
        return Single.just(retVal);
    }
}
