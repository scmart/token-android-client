package com.bakkenbaeck.toshi.view;


import android.app.Application;

import com.bakkenbaeck.toshi.manager.UserManager;

public class BaseApplication extends Application {

    private static BaseApplication instance;
    public static BaseApplication get() { return instance; }

    private UserManager userManager;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        initUserManager();
    }

    private void initUserManager() {
        this.userManager = new UserManager().init();
    }

    public UserManager getUserManager() {
        return this.userManager;
    }
}