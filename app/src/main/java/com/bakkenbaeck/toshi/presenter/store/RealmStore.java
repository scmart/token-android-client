package com.bakkenbaeck.toshi.presenter.store;


import io.realm.Realm;
import io.realm.RealmObject;
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
        final RealmResults<RO> storedObjects = realm.where(clazz).findAll();
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
