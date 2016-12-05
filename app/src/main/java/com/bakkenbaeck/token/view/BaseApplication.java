package com.bakkenbaeck.token.view;


import android.support.multidex.MultiDexApplication;

import com.bakkenbaeck.token.crypto.signal.SignalManager;
import com.bakkenbaeck.token.manager.LocalBalanceManager;
import com.bakkenbaeck.token.manager.UserManager;
import com.bakkenbaeck.token.network.ws.SocketObservables;
import com.bakkenbaeck.token.network.ws.WebSocketManager;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import rx.Single;
import rx.SingleSubscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class BaseApplication extends MultiDexApplication {
    private static BaseApplication instance;
    public static BaseApplication get() { return instance; }

    private UserManager userManager;
    private WebSocketManager webSocketManager;
    private LocalBalanceManager localBalanceManager;
    private SignalManager signalManager;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        init();
    }

    private void init() {
        final Single<UserManager> userCallback = initUserManager();
        initWebsocketManager();
        initRealm();
        initLocalBalanceManager();
        initSignalManager(userCallback);
    }

    private Single<UserManager> initUserManager() {
        this.userManager = new UserManager();
        return this.userManager.init();
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

    private void initLocalBalanceManager() {
        this.localBalanceManager = new LocalBalanceManager();
    }

    private void initSignalManager(final Single<UserManager> userCallback) {
        userCallback
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleSubscriber<UserManager>() {
            @Override
            public void onSuccess(final UserManager userManager) {
                signalManager = new SignalManager().init(userManager.getWallet());
            }

            @Override
            public void onError(final Throwable error) {
                throw new RuntimeException(error);
            }
        });
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

    public void disconnectWebSocket(){
        this.webSocketManager.disconnect();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }
}