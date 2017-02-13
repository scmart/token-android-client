package com.bakkenbaeck.token.presenter.store;


import com.bakkenbaeck.token.model.local.User;

import io.realm.Case;
import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import rx.Single;

public class UserStore {

    public void save(final User user) {
        final Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        realm.insertOrUpdate(user);
        realm.commitTransaction();
        realm.close();
    }

    public Single<RealmResults<User>> loadAll() {
        return Single.fromCallable(() -> {
            final Realm realm = Realm.getDefaultInstance();
            final RealmQuery<User> query = realm.where(User.class);
            final RealmResults<User> results = query.findAll();
            realm.close();
            return results;
        });
    }

    public Single<User> loadForAddress(final String address) {
        return Single.fromCallable(() -> loadWhere("owner_address", address));
    }

    public Single<RealmResults<User>> loadForUsername(final String username) {
        return Single.fromCallable(() -> queryWhere("username", username));
    }

    private User loadWhere(final String fieldName, final String value) {
        final Realm realm = Realm.getDefaultInstance();
        final RealmQuery<User> query = realm.where(User.class);
        query.equalTo(fieldName, value);
        final User result = query.findFirst();
        realm.close();
        return result;
    }

    private RealmResults<User> queryWhere(final String fieldName, final String value) {
        final Realm realm = Realm.getDefaultInstance();
        final RealmQuery<User> query = realm.where(User.class);
        query.contains(fieldName, value, Case.INSENSITIVE);
        final RealmResults<User> results = query.findAll();
        realm.close();
        return results;
    }

}
