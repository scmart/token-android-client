package com.bakkenbaeck.token.presenter.store;


import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.RealmQuery;
import io.realm.RealmResults;

abstract class RealmStore<RO extends RealmObject> {

    private final Realm realm;

    RealmStore() {
        this.realm = Realm.getDefaultInstance();
    }

    public void save(final RO object) {
        this.realm.beginTransaction();
        final RO storedObject = realm.copyToRealm(object);
        this.realm.commitTransaction();
        onNewObject(storedObject);
    }

    public void load(Class<RO> clazz) {
        final RealmQuery<RO> query = realm.where(clazz);
        runAndHandleQuery(query);
    }

    public void loadWhere(Class<RO> clazz, final String fieldName, final String value) {
        final RealmQuery<RO> query = realm.where(clazz);
        query.equalTo(fieldName, value);
        runAndHandleQuery(query);
    }

    private void runAndHandleQuery(final RealmQuery<RO> query) {
        final RealmResults<RO> storedObjects = query.findAll();
        if (storedObjects.size() == 0) {
            onEmptySetAfterLoad();
            return;
        }

        for (final RO object : storedObjects) {
            onNewObject(object);
        }

        onFinishedLoading();
    }

    abstract void onNewObject(RO clazz);
    abstract void onEmptySetAfterLoad();
    abstract void onFinishedLoading();
}
