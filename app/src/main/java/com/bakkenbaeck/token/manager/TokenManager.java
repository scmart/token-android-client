package com.bakkenbaeck.token.manager;


import com.bakkenbaeck.token.crypto.HDWallet;

import rx.Single;

public class TokenManager {
    
    public static final long CACHE_TIMEOUT = 1000 * 60 * 60 * 24;

    private BalanceManager balanceManager;
    private HDWallet wallet;
    private SofaMessageManager sofaMessageManager;
    private TransactionManager transactionManager;
    private UserManager userManager;

    public TokenManager() {
        this.balanceManager = new BalanceManager();
        this.userManager = new UserManager();
        this.sofaMessageManager = new SofaMessageManager();
        this.transactionManager = new TransactionManager();
    }

    public Single<TokenManager> init() {
        return Single.fromCallable(() -> {
            TokenManager.this.wallet = new HDWallet().init();
            TokenManager.this.balanceManager.init(TokenManager.this.wallet);
            TokenManager.this.sofaMessageManager.init(TokenManager.this.wallet);
            TokenManager.this.transactionManager.init(TokenManager.this.wallet);
            TokenManager.this.userManager.init(TokenManager.this.wallet);
            return TokenManager.this;
        });
    }

    public final SofaMessageManager getSofaMessageManager() {
        return this.sofaMessageManager;
    }

    public final TransactionManager getTransactionManager() {
        return this.transactionManager;
    }

    public final UserManager getUserManager() {
        return this.userManager;
    }

    public final BalanceManager getBalanceManager() {
        return this.balanceManager;
    }

    public Single<HDWallet> getWallet() {
        return Single.fromCallable(() -> {
            while (wallet == null) {
                Thread.sleep(200);
            }
            return wallet;
        });
    }

}
