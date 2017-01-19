package com.bakkenbaeck.token.manager;


import com.bakkenbaeck.token.crypto.HDWallet;

import java.util.concurrent.Callable;

import rx.Single;

public class TokenManager {

    private BalanceManager balanceManager;
    private HDWallet wallet;
    private SignalManager signalManager;
    private UserManager userManager;

    public TokenManager() {
        this.balanceManager = new BalanceManager();
        this.userManager = new UserManager();
        this.signalManager = new SignalManager();
    }

    public Single<TokenManager> init() {
        return Single.fromCallable(new Callable<TokenManager>() {
            @Override
            public TokenManager call() throws Exception {
                TokenManager.this.wallet = new HDWallet().init();
                TokenManager.this.balanceManager.init(TokenManager.this.wallet);
                TokenManager.this.signalManager.init(TokenManager.this.wallet);
                TokenManager.this.userManager.init(TokenManager.this.wallet);
                return TokenManager.this;
            }
        });
    }

    public final SignalManager getSignalManager() {
        return this.signalManager;
    }

    public final UserManager getUserManager() {
        return this.userManager;
    }

    public final BalanceManager getBalanceManager() {
        return this.balanceManager;
    }
}
