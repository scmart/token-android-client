/*
 * 	Copyright (c) 2017. Token Browser, Inc
 *
 * 	This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.tokenbrowser.manager;


import com.tokenbrowser.crypto.HDWallet;
import com.tokenbrowser.crypto.signal.SignalPreferences;
import com.tokenbrowser.manager.store.TokenMigration;
import com.tokenbrowser.util.SharedPrefsUtil;
import com.tokenbrowser.view.BaseApplication;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import rx.Completable;
import rx.Single;
import rx.schedulers.Schedulers;
import rx.subjects.BehaviorSubject;

public class TokenManager {

    public static final long CACHE_TIMEOUT = 1000 * 60 * 5;

    private final BehaviorSubject<HDWallet> walletSubject = BehaviorSubject.create();

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
        this.walletSubject.onNext(null);
    }

    public Single<TokenManager> init() {
        if (this.wallet != null && areManagersInitialised) {
            return Single.just(this);
        }
        return new HDWallet()
                .getOrCreateWallet()
                .doOnSuccess(this::setWallet)
                .flatMap(__ -> initManagers())
                .subscribeOn(Schedulers.from(this.singleExecutor));
    }

    public Single<TokenManager> init(final HDWallet wallet) {
        this.setWallet(wallet);
        return initManagers()
                .subscribeOn(Schedulers.from(this.singleExecutor));
    }

    public Single<TokenManager> tryInit() {
        if (this.wallet != null && areManagersInitialised) {
            return Single.just(this);
        }
        return new HDWallet()
                .getExistingWallet()
                .doOnSuccess(this::setWallet)
                .flatMap(__ -> initManagers())
                .subscribeOn(Schedulers.from(this.singleExecutor));
    }

    private void setWallet(final HDWallet wallet) {
        this.wallet = wallet;
        this.walletSubject.onNext(wallet);
    }

    private Single<TokenManager> initManagers() {
        return Single.fromCallable(() -> {
            initRealm();
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

    private void initRealm() {
        final byte[] key = this.wallet.generateDatabaseEncryptionKey();
        Realm.init(BaseApplication.get());
        final RealmConfiguration config = new RealmConfiguration
                .Builder()
                .schemaVersion(8)
                .migration(new TokenMigration(this.wallet))
                .name(this.wallet.getOwnerAddress())
                .encryptionKey(key)
                .build();
        Realm.setDefaultConfiguration(config);
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
        return
                this.walletSubject
                .filter(wallet -> wallet != null)
                .timeout(3, TimeUnit.SECONDS)
                .onErrorReturn(__ -> null)
                .first()
                .toSingle();
    }

    public Completable clearUserData() {
        return Completable.fromAction(() -> {
            this.sofaMessageManager.clear();
            this.userManager.clear();
            this.balanceManager.clear();
            this.transactionManager.clear();
            this.wallet.clear();
            this.areManagersInitialised = false;
            closeDatabase();
            SignalPreferences.clear();
            SharedPrefsUtil.setSignedOut();
            SharedPrefsUtil.clear();
            setWallet(null);
        });
    }

    private void closeDatabase() {
        Realm.removeDefaultConfiguration();
    }
}
