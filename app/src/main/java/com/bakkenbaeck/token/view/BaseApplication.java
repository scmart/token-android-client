package com.bakkenbaeck.token.view;


import android.support.multidex.MultiDexApplication;
import android.util.Log;

import com.bakkenbaeck.token.manager.LocalBalanceManager;
import com.bakkenbaeck.token.manager.UserManager;
import com.bakkenbaeck.token.network.ws.SocketObservables;
import com.bakkenbaeck.token.network.ws.WebSocketManager;

import io.realm.Realm;
import io.realm.RealmConfiguration;

public class BaseApplication extends MultiDexApplication {
    private static final String TAG = "BaseApplication";
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

    public void reconnectWebsocket(){
        this.webSocketManager.requestWebsocketConnection();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }
}