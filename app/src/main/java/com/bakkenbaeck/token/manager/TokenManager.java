package com.bakkenbaeck.token.manager;


import com.bakkenbaeck.token.crypto.HDWallet;

import java.util.concurrent.Callable;

import rx.Single;

public class TokenManager {

    private BalanceManager balanceManager;
    private HDWallet wallet;
    private SignalManager signalManager;
    private UserManager userManager;
    private WebSocketManager webSocketManager;

    public WebSocketManager getWebSocketManager() {
        return webSocketManager;
    }

    public BalanceManager getBalanceManager() {
        return balanceManager;
    }

    public UserManager getUserManager() {
        return userManager;
    }

    public Single<TokenManager> init() {
        this.signalManager = new SignalManager();

        return Single.fromCallable(new Callable<TokenManager>() {
            @Override
            public TokenManager call() throws Exception {
                TokenManager.this.wallet = new HDWallet().init();
                TokenManager.this.balanceManager = new BalanceManager().init(TokenManager.this.wallet);
                TokenManager.this.signalManager.init(TokenManager.this.wallet);
                TokenManager.this.userManager = new UserManager().init(TokenManager.this.wallet);
                TokenManager.this.webSocketManager = new WebSocketManager().init();
                return TokenManager.this;
            }
        });
    }

    public final Single<HDWallet> getWallet() {
        return Single.fromCallable(new Callable<HDWallet>() {
            @Override
            public HDWallet call() throws Exception {
                while(wallet == null) {
                    Thread.sleep(100);
                }
                return wallet;
            }
        });
    }

    public final SignalManager getSignalManager() {
        return this.signalManager;
    }
}
