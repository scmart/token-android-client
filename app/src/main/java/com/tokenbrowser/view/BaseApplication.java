package com.tokenbrowser.view;


import android.content.ComponentCallbacks2;
import android.support.multidex.MultiDexApplication;

import com.tokenbrowser.manager.TokenManager;
import com.tokenbrowser.manager.store.TokenMigration;
import com.tokenbrowser.service.NetworkChangeReceiver;
import com.tokenbrowser.util.LogUtil;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import rx.SingleSubscriber;
import rx.schedulers.Schedulers;
import rx.subjects.BehaviorSubject;

public final class BaseApplication extends MultiDexApplication {

    private static BaseApplication instance;
    public static BaseApplication get() { return instance; }
    private final BehaviorSubject<Boolean> isConnectedSubject = BehaviorSubject.create();

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
        isConnectedSubject().onNext(NetworkChangeReceiver.getCurrentConnectivityStatus(this));
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
                .schemaVersion(1)
                .migration(new TokenMigration())
                .build();
        Realm.setDefaultConfiguration(config);
    }


    public void applicationResumed() {
        if (this.inBackground) {
            this.inBackground = false;
            this.tokenManager.getSofaMessageManager().resumeMessageReceiving();
        }
    }

    @Override
    public void onTrimMemory(final int level) {
        // This is a method for detecting the application going into the background:
        // http://stackoverflow.com/a/19920353
        if (level == ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN) {
            this.inBackground = true;
            this.tokenManager.getSofaMessageManager().disconnect();
        }
        super.onTrimMemory(level);
    }

    public BehaviorSubject<Boolean> isConnectedSubject() {
        return isConnectedSubject;
    }

    public boolean isConnected() {
        return isConnectedSubject.getValue();
    }
}