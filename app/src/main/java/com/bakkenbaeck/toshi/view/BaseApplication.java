package com.bakkenbaeck.toshi.view;


import android.app.Application;

import com.bakkenbaeck.toshi.manager.LocalBalanceManager;
import com.bakkenbaeck.toshi.manager.UserManager;
import com.bakkenbaeck.toshi.network.ws.SocketObservables;
import com.bakkenbaeck.toshi.network.ws.WebSocketManager;

import io.realm.Realm;
import io.realm.RealmConfiguration;

public class BaseApplication extends Application {

    private static BaseApplication instance;
    public static BaseApplication get() { return instance; }

    private UserManager userManager;
    private WebSocketManager webSocketManager;
    private LocalBalanceManager localBalanceManager;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        initUserManager();
        initWebsocketManager();
        initRealm();
        this.localBalanceManager = new LocalBalanceManager();
    }

    private void initUserManager() {
        this.userManager = new UserManager().init();
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

    public SocketObservables getSocketObservables() {
        return this.webSocketManager.getSocketObservables();
    }

    public LocalBalanceManager getLocalBalanceManager() {
        return localBalanceManager;
    }

    public void sendWebSocketMessage(final String message) {
        this.webSocketManager.sendMessage(message);
    }
}