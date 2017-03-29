package com.tokenbrowser.manager;


import android.content.Context;
import android.content.SharedPreferences;

import com.tokenbrowser.crypto.HDWallet;
import com.tokenbrowser.manager.network.CurrencyService;
import com.tokenbrowser.manager.network.EthereumService;
import com.tokenbrowser.model.network.Addresses;
import com.tokenbrowser.model.network.Balance;
import com.tokenbrowser.model.network.GcmRegistration;
import com.tokenbrowser.model.network.MarketRates;
import com.tokenbrowser.model.network.ServerTime;
import com.tokenbrowser.model.sofa.Payment;
import com.tokenbrowser.util.FileNames;
import com.tokenbrowser.util.LocaleUtil;
import com.tokenbrowser.util.LogUtil;
import com.tokenbrowser.view.BaseApplication;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import rx.Single;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subjects.BehaviorSubject;

public class BalanceManager {

    private final static BehaviorSubject<Balance> balanceObservable = BehaviorSubject.create();
    private static final String LAST_KNOWN_BALANCE = "lkb";

    private HDWallet wallet;
    private SharedPreferences prefs;

    /* package */ BalanceManager() {
    }

    public BehaviorSubject<Balance> getBalanceObservable() {
        return balanceObservable;
    }

    public BalanceManager init(final HDWallet wallet) {
        this.wallet = wallet;
        initCachedBalance();
        attachConnectivityObserver();
        return this;
    }

    private void initCachedBalance() {
        this.prefs = BaseApplication.get().getSharedPreferences(FileNames.BALANCE_PREFS, Context.MODE_PRIVATE);
        final Balance cachedBalance = new Balance(readLastKnownBalance());
        handleNewBalance(cachedBalance);
    }

    private void attachConnectivityObserver() {
        BaseApplication
                .get()
                .isConnectedSubject()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(unused -> this.refreshBalance());
    }

    public void refreshBalance() {
            EthereumService
                .getApi()
                .getBalance(this.wallet.getPaymentAddress())
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(this::handleNewBalance, this::handleError);
    }

    private void handleNewBalance(final Balance balance) {
        writeLastKnownBalance(balance);
        balanceObservable.onNext(balance);
    }

    private void handleError(final Throwable throwable) {
        LogUtil.e(getClass(), throwable.toString());
    }


    private Single<MarketRates> getRates() {
        return fetchLatestRates()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    private Single<MarketRates> fetchLatestRates() {
        return CurrencyService
                .getApi()
                .getRates("ETH")
                .onErrorReturn(__ -> new MarketRates());
    }

    // Currently hard-coded to USD
    public Single<String> convertEthToLocalCurrencyString(final BigDecimal ethAmount) {
         return getRates().map((marketRates) -> {
             final BigDecimal marketRate = marketRates.getRate("USD");
             final BigDecimal localAmount = marketRate.multiply(ethAmount);

             final NumberFormat numberFormat = NumberFormat.getNumberInstance(LocaleUtil.getLocale());
             numberFormat.setGroupingUsed(true);
             numberFormat.setMaximumFractionDigits(2);
             numberFormat.setMinimumFractionDigits(2);

             final String localAmountAsString = numberFormat.format(localAmount);
             return "$" + localAmountAsString + " USD";
         });
    }

    // Currently hard-coded to USD
    public Single<BigDecimal> convertEthToLocalCurrency(final BigDecimal ethAmount) {
        return getRates().map((marketRates) -> {
            final BigDecimal marketRate = marketRates.getRate("USD");
            return marketRate.multiply(ethAmount);
        });
    }

    // Currently hard-coded to USD
    public Single<BigDecimal> convertLocalCurrencyToEth(final BigDecimal localAmount) {
        return getRates().map((marketRates) -> {
            if (localAmount.compareTo(BigDecimal.ZERO) == 0) {
                return BigDecimal.ZERO;
            }

            final BigDecimal marketRate = marketRates.getRate("USD");
            if (marketRate.compareTo(BigDecimal.ZERO) == 0) {
                return BigDecimal.ZERO;
            }
            return localAmount.divide(marketRate, 8, RoundingMode.HALF_DOWN);
        });
    }


    public Single<Void> registerForGcm(final String token) {
        return EthereumService
                .getApi()
                .getTimestamp()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .flatMap((st) -> registerForGcmWithTimestamp(token, st));
    }

    public Single<Void> unregisterFromGcm(final String token) {
        return EthereumService
                .getApi()
                .getTimestamp()
                .subscribeOn(Schedulers.io())
                .flatMap((st) -> unregisterGcmWithTimestamp(token, st));
    }

    private Single<Void> registerForGcmWithTimestamp(final String token, final ServerTime serverTime) {
        if (serverTime == null) {
            throw new IllegalStateException("ServerTime was null");
        }

        return EthereumService
                .getApi()
                .registerGcm(serverTime.get(), new GcmRegistration(token));
    }

    private Single<Void> unregisterGcmWithTimestamp(final String token, final ServerTime serverTime) {
        if (serverTime == null) {
            throw new IllegalStateException("ServerTime was null");
        }

        return EthereumService
                .getApi()
                .unregisterGcm(serverTime.get(), new GcmRegistration(token));
    }

    public Single<Void> watchForWalletTransactions() {
        return EthereumService
                .getApi()
                .getTimestamp()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .flatMap(this::watchForWalletTransactionsWithTimestamp);
    }

    private Single<Void> watchForWalletTransactionsWithTimestamp(final ServerTime serverTime) {
        if (serverTime == null) {
            throw new IllegalStateException("ServerTime was null");
        }

        final List<String> list = new ArrayList<>();
        list.add(wallet.getPaymentAddress());
        final Addresses addresses = new Addresses(list);

        return EthereumService
                .getApi()
                .startWatchingAddresses(serverTime.get(), addresses);
    }

    /* package */ Single<Payment> getTransactionStatus(final String transactionHash) {
        return EthereumService
                .get()
                .getStatusOfTransaction(transactionHash);
    }

    private String readLastKnownBalance() {
        return this.prefs
                .getString(LAST_KNOWN_BALANCE, "0x0");
    }

    private void writeLastKnownBalance(final Balance balance) {
        this.prefs
                .edit()
                .putString(LAST_KNOWN_BALANCE, balance.getUnconfirmedBalanceAsHex())
                .apply();
    }

    public void clear() {
        this.prefs
                .edit()
                .clear()
                .apply();
    }
}