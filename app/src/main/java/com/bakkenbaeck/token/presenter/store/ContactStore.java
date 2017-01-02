package com.bakkenbaeck.token.presenter.store;


import com.bakkenbaeck.token.model.Contact;

import java.util.concurrent.Callable;

import io.realm.Realm;
import io.realm.RealmQuery;
import rx.Single;

public class ContactStore {

    private final Realm realm;

    public ContactStore() {
        this.realm = Realm.getDefaultInstance();
    }

    public Single<Contact> load(final String address) {
        return Single.fromCallable(new Callable<Contact>() {
            @Override
            public Contact call() throws Exception {
                return loadWhere("address", address);
            }
        });
    }

    public void save(final Contact contact) {
        this.realm.beginTransaction();
        this.realm.insert(contact);
        this.realm.commitTransaction();
    }

    private Contact loadWhere(final String fieldName, final String value) {
        final RealmQuery<Contact> query = realm.where(Contact.class);
        query.equalTo(fieldName, value);
        return query.findFirst();
    }
}
