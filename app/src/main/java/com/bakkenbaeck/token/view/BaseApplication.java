package com.bakkenbaeck.token.view;


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
        new TokenManager().init()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(new SingleSubscriber<TokenManager>() {
                    @Override
                    public void onSuccess(final TokenManager tokenManager) {
                        BaseApplication.this.tokenManager = tokenManager;
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

}