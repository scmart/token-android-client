package com.bakkenbaeck.toshi.view;


import android.app.Application;

import com.bakkenbaeck.toshi.manager.UserManager;
import com.bakkenbaeck.toshi.manager.WalletManager;
import com.bakkenbaeck.toshi.network.ws.WebSocketManager;
import com.bakkenbaeck.toshi.model.User;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import rx.Subscriber;

public class BaseApplication extends Application {

    private static BaseApplication instance;
    public static BaseApplication get() { return instance; }

    private UserManager userManager;
    private WalletManager walletManager;
    private WebSocketManager webSocketManager;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        initUserManager();
        initWalletManager();
        initWebsocketManager();
        initRealm();
    }

    private void initUserManager() {
        this.userManager = new UserManager().init();
        this.userManager.getObservable().subscribe(this.currentUserSubscriber);
    }

    private void initWalletManager() {
        this.walletManager = new WalletManager().init();
    }

    private void initWebsocketManager() {
        this.webSocketManager = new WebSocketManager();
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

    private final Subscriber<User> currentUserSubscriber = new Subscriber<User>() {
        @Override
        public void onCompleted() {}

        @Override
        public void onError(final Throwable e) {}

        @Override
        public void onNext(final User user) {
            this.unsubscribe();
            webSocketManager.init(user.getId());
        }
    };
}