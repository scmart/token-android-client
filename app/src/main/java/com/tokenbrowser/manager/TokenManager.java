package com.tokenbrowser.manager;


import com.tokenbrowser.crypto.HDWallet;

import rx.Single;

public class TokenManager {
    
    public static final long CACHE_TIMEOUT = 1000 * 60 * 60 * 24;

    private AppsManager appsManager;
    private BalanceManager balanceManager;
    private HDWallet wallet;
    private SofaMessageManager sofaMessageManager;
    private TransactionManager transactionManager;
    private UserManager userManager;
    private ReputationManager reputationManager;

    public TokenManager() {
        this.appsManager = new AppsManager();
        this.balanceManager = new BalanceManager();
        this.userManager = new UserManager();
        this.sofaMessageManager = new SofaMessageManager();
        this.transactionManager = new TransactionManager();
    }

    public Single<TokenManager> init() {
        return Single.fromCallable(() -> {
            TokenManager.this.wallet = new HDWallet().init();
            TokenManager.this.appsManager.init();
            TokenManager.this.balanceManager.init(TokenManager.this.wallet);
            TokenManager.this.sofaMessageManager.init(TokenManager.this.wallet);
            TokenManager.this.transactionManager.init(TokenManager.this.wallet);
            TokenManager.this.userManager.init(TokenManager.this.wallet);
            TokenManager.this.reputationManager = new ReputationManager();
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

    public final AppsManager getAppsManager() {
        return this.appsManager;
    }

    public final ReputationManager getReputationManager() {
        return this.reputationManager;
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
