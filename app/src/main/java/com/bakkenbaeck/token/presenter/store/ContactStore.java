package com.bakkenbaeck.token.presenter.store;


import com.bakkenbaeck.token.model.local.User;

import java.util.concurrent.Callable;

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
        this.realm.beginTransaction();
        this.realm.insertOrUpdate(user);
        this.realm.commitTransaction();
    }

    public Single<RealmResults<User>> loadAll() {
        return Single.fromCallable(new Callable<RealmResults<User>>() {
            @Override
            public RealmResults<User> call() throws Exception {
                final RealmQuery<User> query = realm.where(User.class);
                return query.findAll();
            }
        });
    }

    public Single<User> load(final String address) {
        return Single.fromCallable(new Callable<User>() {
            @Override
            public User call() throws Exception {
                return loadWhere("owner_address", address);
            }
        });
    }

    private User loadWhere(final String fieldName, final String value) {
        final RealmQuery<User> query = realm.where(User.class);
        query.equalTo(fieldName, value);
        return query.findFirst();
    }


}
