package com.tokenbrowser.manager.store;


import com.tokenbrowser.model.local.Contact;
import com.tokenbrowser.model.local.User;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmQuery;
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
        final List<Contact> results = realm.copyFromRealm(query.findAll());
        realm.close();
        return Single.just(results);
    }
}
