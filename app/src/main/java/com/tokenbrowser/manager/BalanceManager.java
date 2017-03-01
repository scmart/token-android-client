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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import rx.Single;
import rx.schedulers.Schedulers;
import rx.subjects.BehaviorSubject;

public class BalanceManager {

    private final static BehaviorSubject<Balance> balanceObservable = BehaviorSubject.create();

    private HDWallet wallet;
    private Balance balance;
    private MarketRates rates;

    /* package */ BalanceManager() {
        this.balance = new Balance();
        this.rates = new MarketRates();
    }

    public BalanceManager init(final HDWallet wallet) {
        this.wallet = wallet;
        refreshBalance();
        getMarketRates();
        return this;
    }

    public void refreshBalance() {
            BalanceService
                .getApi()
                .getBalance(this.wallet.getPaymentAddress())
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(this::handleNewBalance, this::handleError);
    }

    private void handleError(final Throwable throwable) {
        LogUtil.e(getClass(), throwable.toString());
    }

    private void handleNewBalance(final Balance balance) {
        this.balance = balance;
        final BigDecimal ethAmount = EthUtil.weiToEth(this.balance.getUnconfirmedBalance());
        this.balance.setFormattedLocalBalance(convertEthToLocalCurrencyString(ethAmount));

        balanceObservable.onNext(balance);
    }

    public BehaviorSubject<Balance> getBalanceObservable() {
        return balanceObservable;
    }

    private void getMarketRates() {
        CurrencyService
                .getApi()
                .getRates("ETH")
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(this::handleMarketRates, this::handleError);
    }

    private void handleMarketRates(final MarketRates rates) {
        this.rates = rates;
        // Rebroadcast the balance now that we have new rates.
        handleNewBalance(this.balance);
    }

    // Currently hard-coded to USD
    public String convertEthToLocalCurrencyString(final BigDecimal ethAmount) {
        final BigDecimal marketRate = getEthMarketRate("USD");
        final BigDecimal localAmount = marketRate.multiply(ethAmount);

        final NumberFormat numberFormat = NumberFormat.getNumberInstance(LocaleUtil.getLocale());
        numberFormat.setGroupingUsed(true);
        numberFormat.setMaximumFractionDigits(2);
        numberFormat.setMinimumFractionDigits(2);

        final String localAmountAsString = numberFormat.format(localAmount);
        return "$" + localAmountAsString + " USD";
    }

    // Currently hard-coded to USD
    public BigDecimal convertEthToLocalCurrency(final BigDecimal ethAmount) {
        final BigDecimal marketRate = getEthMarketRate("USD");
        return marketRate.multiply(ethAmount);
    }

    // Currently hard-coded to USD
    public BigDecimal convertLocalCurrencyToEth(final BigDecimal localAmount) {
        if (localAmount.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        final BigDecimal marketRate = getEthMarketRate("USD");
        if (marketRate.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return localAmount.divide(marketRate, 8, RoundingMode.HALF_DOWN);
    }

    // Get the value of ethereum in another currency
    private BigDecimal getEthMarketRate(final String currency) {
        return this.rates.getRate(currency);
    }

    public Single<Void> registerForGcm(final String token, final boolean forceUpdate) {
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