package com.tokenbrowser.manager;


import com.tokenbrowser.crypto.HDWallet;
import com.tokenbrowser.crypto.signal.SignalPreferences;
import com.tokenbrowser.util.SharedPrefsUtil;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import rx.Completable;
import rx.Single;
import rx.schedulers.Schedulers;

public class TokenManager {

    public static final long CACHE_TIMEOUT = 1000 * 60 * 60 * 24;

    private AppsManager appsManager;
    private BalanceManager balanceManager;
    private HDWallet wallet;
    private SofaMessageManager sofaMessageManager;
    private TransactionManager transactionManager;
    private UserManager userManager;
    private ReputationManager reputationManager;
    private ExecutorService singleExecutor;
    private boolean areManagersInitialised = false;

    public TokenManager() {
        this.singleExecutor = Executors.newSingleThreadExecutor();
        this.appsManager = new AppsManager();
        this.balanceManager = new BalanceManager();
        this.userManager = new UserManager();
        this.sofaMessageManager = new SofaMessageManager();
        this.transactionManager = new TransactionManager();
    }

    public Single<TokenManager> init() {
        if (this.wallet != null && areManagersInitialised) {
            return Single.just(this);
        }
        return new HDWallet()
                .getOrCreateWallet()
                .doOnSuccess(wallet -> this.wallet = wallet)
                .flatMap(__ -> initManagers())
                .subscribeOn(Schedulers.from(this.singleExecutor));
    }

    public Single<TokenManager> init(final HDWallet wallet) {
        this.wallet = wallet;
        return initManagers()
                .subscribeOn(Schedulers.from(this.singleExecutor));
    }

    public Single<TokenManager> tryInit() {
        if (this.wallet != null && areManagersInitialised) {
            return Single.just(this);
        }
        return new HDWallet()
                .getExistingWallet()
                .doOnSuccess(wallet -> this.wallet = wallet)
                .flatMap(__ -> initManagers())
                .subscribeOn(Schedulers.from(this.singleExecutor));
    }

    private Single<TokenManager> initManagers() {
        return Single.fromCallable(() -> {
            this.appsManager.init();
            this.balanceManager.init(this.wallet);
            this.sofaMessageManager.init(this.wallet);
            this.transactionManager.init(this.wallet);
            this.userManager.init(this.wallet);
            this.reputationManager = new ReputationManager();
            this.areManagersInitialised = true;
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
        return Completable.fromAction(() -> {
            this.sofaMessageManager.clear();
            this.userManager.clear();
            this.balanceManager.clear();
            this.transactionManager.clear();
            this.wallet.clear();
            this.wallet = null;
            this.areManagersInitialised = false;
            SignalPreferences.clear();
            SharedPrefsUtil.setSignedOut();
            SharedPrefsUtil.clear();
        });
    }
}
