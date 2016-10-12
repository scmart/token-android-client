package com.bakkenbaeck.toshi.presenter.store;


import android.util.Log;

import java.util.Date;

import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.RealmResults;
import io.realm.Sort;

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

    public void loadByDates(Class<RO> clazz){
        final RealmResults<RO> storedObjects1 = realm.where(clazz).findAll().sort("creationTime", Sort.DESCENDING);
        if(storedObjects1.size() > 0){
            onNewDate(storedObjects1.get(0));
        }
    }

    abstract void onNewDate(RO clazz);
    abstract void onNewObject(RO clazz);
    abstract void onEmptySetAfterLoad();
    abstract void onFinishedLoading();
}
