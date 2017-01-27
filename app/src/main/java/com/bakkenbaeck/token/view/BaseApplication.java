package com.bakkenbaeck.token.view;


import android.content.ComponentCallbacks2;
import android.support.multidex.MultiDexApplication;

import com.bakkenbaeck.token.manager.TokenManager;
import com.bakkenbaeck.token.util.LogUtil;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import rx.SingleSubscriber;
import rx.schedulers.Schedulers;

public final class BaseApplication extends MultiDexApplication {

    private static BaseApplication instance;
    public static BaseApplication get() { return instance; }

    private TokenManager tokenManager;
    private boolean inBackground = false;

    public final TokenManager getTokenManager() {
        return this.tokenManager;
    }

    @Override
    public final void onCreate() {
        super.onCreate();
        instance = this;
        init();
    }

    private void init() {
        initTokenManager();
        initRealm();
    }

    private void initTokenManager() {
        this.tokenManager = new TokenManager();
        this.tokenManager.init()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(new SingleSubscriber<TokenManager>() {
                    @Override
                    public void onSuccess(final TokenManager tokenManager) {
                        this.unsubscribe();
                    }

                    @Override
                    public void onError(final Throwable error) {
                        this.unsubscribe();
                        LogUtil.e(getClass(), "Fundamental error setting up managers. " + error);
                        throw new RuntimeException(error);
                    }
                });
    }


    private void initRealm() {
        Realm.init(this);
        final RealmConfiguration config = new RealmConfiguration
                .Builder()
                .deleteRealmIfMigrationNeeded()
                .build();
        Realm.setDefaultConfiguration(config);
    }


    public void applicationResumed() {
        if (this.inBackground) {
            this.inBackground = false;
            this.tokenManager.getChatMessageManager().resumeMessageReceiving();
        }
    }

    @Override
    public void onTrimMemory(final int level) {
        // This is a method for detecting the application going into the background:
        // http://stackoverflow.com/a/19920353
        if (level == ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN) {
            this.inBackground = true;
            this.tokenManager.getChatMessageManager().disconnect();
        }
        super.onTrimMemory(level);
    }
}