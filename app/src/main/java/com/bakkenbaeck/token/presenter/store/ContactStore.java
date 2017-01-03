package com.bakkenbaeck.token.presenter.store;


import com.bakkenbaeck.token.model.User;

import java.util.concurrent.Callable;

import io.realm.Realm;
import io.realm.RealmQuery;
import rx.Single;

public class ContactStore {

    private final Realm realm;

    public ContactStore() {
        this.realm = Realm.getDefaultInstance();
    }

    public Single<User> load(final String address) {
        return Single.fromCallable(new Callable<User>() {
            @Override
            public User call() throws Exception {
                return loadWhere("address", address);
            }
        });
    }

    public void save(final User user) {
        this.realm.beginTransaction();
        this.realm.insertOrUpdate(user);
        this.realm.commitTransaction();
    }

    private User loadWhere(final String fieldName, final String value) {
        final RealmQuery<User> query = realm.where(User.class);
        query.equalTo(fieldName, value);
        return query.findFirst();
    }
}
