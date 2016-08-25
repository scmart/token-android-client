package com.bakkenbaeck.toshi.view.adapter;


import android.support.v7.widget.RecyclerView;

import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.RealmResults;

public abstract class DatabaseBackedAdapter<RO extends RealmObject, VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {

    private final Realm realm;

    public DatabaseBackedAdapter() {
        this.realm = Realm.getDefaultInstance();
    }

    public RO saveObjectToDatabase(final RO object) {
        this.realm.beginTransaction();
        final RO storedObject = realm.copyToRealm(object);
        this.realm.commitTransaction();
        return storedObject;
    }

    public void getStoredObjects(Class<RO> clazz) {
        final RealmResults<RO> storedObjects = realm.where(clazz).findAll();
        if (storedObjects.size() == 0) {
            onEmptySet();
            return;
        }

        for (final RO object : storedObjects) {
            onObjectLoaded(object);
        }

        onFinishedLoadingAllObjects();
    }

    abstract void onObjectLoaded(RO clazz);
    abstract void onEmptySet();
    abstract void onFinishedLoadingAllObjects();
}
