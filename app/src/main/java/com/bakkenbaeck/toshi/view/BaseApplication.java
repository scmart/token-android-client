package com.bakkenbaeck.toshi.view;


import android.app.Application;

import com.bakkenbaeck.toshi.manager.UserManager;

import io.realm.Realm;
import io.realm.RealmConfiguration;

public class BaseApplication extends Application {

    private static BaseApplication instance;
    public static BaseApplication get() { return instance; }

    private UserManager userManager;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        initUserManager();
        initRealm();
    }

    private void initUserManager() {
        this.userManager = new UserManager().init();
    }

    private void initRealm() {
        final RealmConfiguration config = new RealmConfiguration
                .Builder(this)
                .deleteRealmIfMigrationNeeded()
                .build();
        Realm.setDefaultConfiguration(config);
    }

    public UserManager getUserManager() {
        return this.userManager;
    }
}