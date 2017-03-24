package com.tokenbrowser.manager;


import com.tokenbrowser.crypto.HDWallet;
import com.tokenbrowser.crypto.signal.SignalPreferences;
import com.tokenbrowser.util.SharedPrefsUtil;

import rx.Completable;
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
        return new HDWallet().getOrCreateWallet()
                .doOnSuccess(wallet -> this.wallet = wallet)
                .flatMap(unused -> initManagers());
    }

    public Single<TokenManager> init(final HDWallet wallet) {
        this.wallet = wallet;
        return initManagers();
    }

    private Single<TokenManager> initManagers() {
        return Single.fromCallable(() -> {
            this.appsManager.init();
            this.balanceManager.init(this.wallet);
            this.sofaMessageManager.init(this.wallet);
            this.transactionManager.init(this.wallet);
            this.userManager.init(this.wallet);
            this.reputationManager = new ReputationManager();
            return this;
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

    public Completable clearUserData() {
        return Completable.fromCallable(() -> {
            this.sofaMessageManager.clear();
            this.userManager.clear();
            this.balanceManager.clear();
            this.wallet.clear();
            this.wallet = null;
            SignalPreferences.clear();
            SharedPrefsUtil.setSignedOut();
            SharedPrefsUtil.clear();
            return null;
        });
    }
}
