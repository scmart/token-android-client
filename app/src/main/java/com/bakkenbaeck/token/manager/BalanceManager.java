package com.bakkenbaeck.token.manager;


import com.bakkenbaeck.token.crypto.HDWallet;
import com.bakkenbaeck.token.model.network.Balance;
import com.bakkenbaeck.token.model.network.MarketRates;
import com.bakkenbaeck.token.network.BalanceService;
import com.bakkenbaeck.token.network.CurrencyService;
import com.bakkenbaeck.token.util.EthUtil;
import com.bakkenbaeck.token.util.LocaleUtil;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;

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
                .subscribe(this::handleNewBalance);
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
                .subscribe(this::handleMarketRates);
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
}