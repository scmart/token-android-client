package com.tokenbrowser.manager.store;


import com.tokenbrowser.model.local.User;

import java.util.List;

import io.realm.Case;
import io.realm.Realm;
import io.realm.RealmQuery;
import rx.Observable;
import rx.Single;

public class UserStore {

    public Observable<User> loadForAddress(final String address) {
        return Observable.fromCallable(() -> loadWhere("owner_address", address));
    }

    public User loadForPaymentAddress(final String address) {
        return loadWhere("payment_address", address);
    }

    public Single<List<User>> queryUsername(final String query) {
        return Single.fromCallable(() -> filter("username", query));
    }

    public void save(final User user) {
        final Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        realm.insertOrUpdate(user);
        realm.commitTransaction();
        realm.close();
    }

    private User loadWhere(final String fieldName, final String value) {
        final Realm realm = Realm.getDefaultInstance();
        final User user =
                realm.where(User.class)
                .equalTo(fieldName, value)
                .findFirst();

        final User retVal = user == null ? null : realm.copyFromRealm(user);
        realm.close();
        return retVal;
    }

    private List<User> filter(final String fieldName, final String value) {
        final Realm realm = Realm.getDefaultInstance();
        final RealmQuery<User> query = realm.where(User.class);
        query.contains(fieldName, value, Case.INSENSITIVE);
        final List<User> result = realm.copyFromRealm(query.findAll());
        realm.close();
        return result;
    }

    public void clearRealm() {
        final Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        realm.deleteAll();
        realm.commitTransaction();
        realm.close();
    }
}
