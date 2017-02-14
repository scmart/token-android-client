package com.bakkenbaeck.token.presenter.store;


import com.bakkenbaeck.token.model.local.Contact;
import com.bakkenbaeck.token.model.local.User;

import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import rx.Single;

public class ContactStore {

    private final Realm realm;

    public ContactStore() {
        this.realm = Realm.getDefaultInstance();
    }

    public void save(final User user) {
        realm.beginTransaction();
        final User storedUser = realm.copyToRealmOrUpdate(user);
        final Contact contact = new Contact(storedUser);
        realm.insert(contact);
        realm.commitTransaction();
    }

    public Single<RealmResults<Contact>> loadAll() {
        return Single.fromCallable(() -> {
            final RealmQuery<Contact> query = realm.where(Contact.class);
            return query.findAll();
        });
    }
}
