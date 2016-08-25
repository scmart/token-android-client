package com.bakkenbaeck.toshi.view.adapter;


import android.support.v7.widget.RecyclerView;

import io.realm.Realm;
import io.realm.RealmObject;

public abstract class DatabaseBackedAdapter<VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {

    private final Realm realm;

    public DatabaseBackedAdapter() {
        this.realm = Realm.getDefaultInstance();
    }

    public void saveObjectToDatabase(final RealmObject object) {
        this.realm.beginTransaction();
        realm.copyToRealm(object);
        this.realm.commitTransaction();
    }
}
