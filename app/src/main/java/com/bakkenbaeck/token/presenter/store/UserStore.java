package com.bakkenbaeck.token.presenter.store;


import com.bakkenbaeck.token.model.local.User;

import io.realm.Case;
import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import rx.Single;

public class UserStore {

    private final Realm realm;

    public UserStore() {
        this.realm = Realm.getDefaultInstance();
    }

    public void save(final User user) {
        realm.beginTransaction();
        realm.insertOrUpdate(user);
        realm.commitTransaction();
    }

    public Single<RealmResults<User>> loadAll() {
        return Single.fromCallable(() -> {
            final RealmQuery<User> query = realm.where(User.class);
            return query.findAll();
        });
    }

    public Single<User> loadForAddress(final String address) {
        return Single.fromCallable(() -> loadWhere("owner_address", address));
    }

    public Single<RealmResults<User>> loadForUsername(final String username) {
        return Single.fromCallable(() -> queryWhere("username", username));
    }

    private User loadWhere(final String fieldName, final String value) {
        final RealmQuery<User> query = realm.where(User.class);
        query.equalTo(fieldName, value);
        return query.findFirst();
    }

    private RealmResults<User> queryWhere(final String fieldName, final String value) {
        final RealmQuery<User> query = realm.where(User.class);
        query.contains(fieldName, value, Case.INSENSITIVE);
        return query.findAll();
    }

}
