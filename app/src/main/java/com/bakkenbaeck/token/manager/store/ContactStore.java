package com.bakkenbaeck.token.manager.store;


import com.bakkenbaeck.token.model.local.Contact;
import com.bakkenbaeck.token.model.local.User;

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
                .equalTo("owner_address", user.getOwnerAddress())
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
                .equalTo("owner_address", user.getOwnerAddress())
                .findFirst()
                .deleteFromRealm();
        realm.commitTransaction();
    }

    public Single<RealmResults<Contact>> loadAll() {
        return Single.fromCallable(() -> {
            final RealmQuery<Contact> query = realm.where(Contact.class);
            return query.findAll();
        });
    }
}
