package com.tokenbrowser.manager;


import com.tokenbrowser.crypto.HDWallet;
import com.tokenbrowser.manager.network.BalanceService;
import com.tokenbrowser.manager.network.CurrencyService;
import com.tokenbrowser.model.network.Addresses;
import com.tokenbrowser.model.network.Balance;
import com.tokenbrowser.model.network.GcmRegistration;
import com.tokenbrowser.model.network.MarketRates;
import com.tokenbrowser.model.network.ServerTime;
import com.tokenbrowser.util.EthUtil;
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

    private HDWallet wallet;
    private Balance balance;
    private MarketRates rates;

    /* package */ BalanceManager() {
        this.balance = new Balance();
    }

    public BehaviorSubject<Balance> getBalanceObservable() {
        return balanceObservable;
    }

    public BalanceManager init(final HDWallet wallet) {
        this.wallet = wallet;
        attachConnectivityObserver();
        return this;
    }

    private void attachConnectivityObserver() {
        BaseApplication
                .get()
                .isConnectedSubject()
                .filter(isConnected -> isConnected)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(isConnected -> refreshBalance());
    }

    public void refreshBalance() {
            BalanceService
                .getApi()
                .getBalance(this.wallet.getPaymentAddress())
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(this::handleNewBalance, this::handleError);
    }

    private void handleNewBalance(final Balance balance) {
        this.balance = balance;
        final BigDecimal ethAmount = EthUtil.weiToEth(this.balance.getUnconfirmedBalance());

        convertEthToLocalCurrencyString(ethAmount)
        .subscribe((newBalance) -> {
            this.balance.setFormattedLocalBalance(newBalance);
            balanceObservable.onNext(balance);
        });
    }

    private void handleError(final Throwable throwable) {
        LogUtil.e(getClass(), throwable.toString());
    }


    private Single<MarketRates> getRates() {
        return Single
                .concat(
                    Single.just(this.rates),
                        fetchAndCacheLatestRates())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .first(rates -> rates != null && !rates.needsRefresh())
                .toSingle();
    }

    private Single<MarketRates> fetchAndCacheLatestRates() {
        return CurrencyService
                .getApi()
                .getRates("ETH")
                .doOnSuccess((rates) -> this.rates = rates);
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
        return BalanceService
                .getApi()
                .getTimestamp()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .flatMap((st) -> registerForGcmWithTimestamp(token, st));
    }

    private Single<Void> registerForGcmWithTimestamp(final String token, final ServerTime serverTime) {
        if (serverTime == null) {
            throw new IllegalStateException("ServerTime was null");
        }

        return BalanceService
                .getApi()
                .registerGcm(serverTime.get(), new GcmRegistration(token));
    }

    public Single<Void> watchForWalletTransactions() {
        return BalanceService
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

        return BalanceService
                .getApi()
                .startWatchingAddresses(serverTime.get(), addresses);
    }
}