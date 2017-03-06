package com.tokenbrowser.manager.store;


import com.tokenbrowser.model.local.User;

import java.util.List;

import io.realm.Case;
import io.realm.Realm;
import io.realm.RealmQuery;
import rx.Observable;
import rx.Single;

public class UserStore {

    private final Realm realm;

    public UserStore() {
        this.realm = Realm.getDefaultInstance();
    }

    public Observable<User> loadForAddress(final String address) {
        return Observable.fromCallable(() -> loadWhere("token_id", address));
    }

    public User loadForPaymentAddress(final String address) {
        return loadWhere("payment_address", address);
    }

    public Single<List<User>> queryUsername(final String query) {
        return Single.fromCallable(() -> filter("username", query));
    }

    public void save(final User user) {
        realm.beginTransaction();
        realm.insertOrUpdate(user);
        realm.commitTransaction();
    }

    private User loadWhere(final String fieldName, final String value) {
        final User user =
                realm.where(User.class)
                .equalTo(fieldName, value)
                .findFirst();

        if (user == null) {
           return null;
        }

        return realm.copyFromRealm(user);
    }

    private List<User> filter(final String fieldName, final String value) {
        final RealmQuery<User> query = realm.where(User.class);
        query.contains(fieldName, value, Case.INSENSITIVE);
        return realm.copyFromRealm(query.findAll());
    }

}
