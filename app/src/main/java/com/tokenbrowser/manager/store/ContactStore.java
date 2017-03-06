package com.tokenbrowser.manager.store;


import com.tokenbrowser.model.local.Contact;
import com.tokenbrowser.model.local.User;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmQuery;
import rx.Single;

public class ContactStore {

    private final Realm realm;

    public ContactStore() {
        this.realm = Realm.getDefaultInstance();
    }

    public boolean userIsAContact(final User user) {
        return realm
                .where(Contact.class)
                .equalTo("owner_address", user.getTokenId())
                .findFirst() != null;
    }

    public void save(final User user) {
        realm.beginTransaction();
        final User storedUser = realm.copyToRealmOrUpdate(user);
        final Contact contact = new Contact(storedUser);
        realm.insert(contact);
        realm.commitTransaction();
    }

    public void delete(final User user) {
        realm.beginTransaction();
        realm
                .where(Contact.class)
                .equalTo("owner_address", user.getTokenId())
                .findFirst()
                .deleteFromRealm();
        realm.commitTransaction();
    }

    public Single<List<Contact>> loadAll() {
        return Single.fromCallable(() -> {
            final RealmQuery<Contact> query = realm.where(Contact.class);
            return realm.copyFromRealm(query.findAll());
        });
    }
}
