package com.bakkenbaeck.token.manager;


import com.bakkenbaeck.token.crypto.HDWallet;
import com.bakkenbaeck.token.model.network.Balance;
import com.bakkenbaeck.token.model.network.MarketRates;
import com.bakkenbaeck.token.network.BalanceService;
import com.bakkenbaeck.token.network.CurrencyService;
import com.bakkenbaeck.token.util.EthUtil;
import com.bakkenbaeck.token.util.SingleSuccessSubscriber;

import java.math.BigDecimal;
import java.math.BigInteger;

import rx.schedulers.Schedulers;

public class BalanceManager {

    private HDWallet wallet;
    private Balance balance;
    private MarketRates rates;

    public BalanceManager() {
        this.balance = new Balance();
        this.rates = new MarketRates();
    }

    public BalanceManager init(final HDWallet wallet) {
        this.wallet = wallet;
        syncBalanceWithServer(wallet);
        getMarketRates();
        return this;
    }

    private void syncBalanceWithServer(final HDWallet wallet) {
            BalanceService.getApi()
                .getBalance(wallet.getWalletAddress())
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(new SingleSuccessSubscriber<Balance>() {
                    @Override
                    public void onSuccess(final Balance balance) {
                        BalanceManager.this.balance = balance;
                        this.unsubscribe();
                    }
                });
    }

    private void getMarketRates() {
        CurrencyService
                .getApi()
                .getRates("ETH")
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(new SingleSuccessSubscriber<MarketRates>() {
                    @Override
                    public void onSuccess(final MarketRates rates) {
                        BalanceManager.this.rates = rates;
                    }
                });
    }

    private BigInteger transferFee = new BigInteger("420000000000000");
    private double ethValue;

    public BigInteger getConfirmedBalance() {
        return this.balance.getConfirmedBalance();
    }

    public BigDecimal getConfirmedBalanceAsEth() {
        return EthUtil.weiToEth(this.balance.getConfirmedBalance());
    }

    public BigDecimal getConfirmedBalanceAsEthMinusTransferFee() {
        if (this.balance.getConfirmedBalance() == null || this.balance.getConfirmedBalance().subtract(transferFee).compareTo(BigInteger.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        return EthUtil.weiToEth(this.balance.getConfirmedBalance().subtract(transferFee));
    }

    public BigDecimal getUnconfirmedBalanceAsEthMinusTransferFee() {
        if (this.balance.getUnconfirmedBalance() == null || this.balance.getUnconfirmedBalance().subtract(transferFee).compareTo(BigInteger.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        return EthUtil.weiToEth(this.balance.getUnconfirmedBalance().subtract(transferFee));
    }

    public String confirmedBalanceString() {
        if (this.balance.getConfirmedBalance() == null || this.balance.getConfirmedBalance().equals(BigInteger.ZERO)) {
            return "0";
        }
        return EthUtil.weiToEthString(this.balance.getConfirmedBalance());
    }

    public String confirmedBalanceStringMinusTransferFee() {
        if (this.balance.getConfirmedBalance() == null || this.balance.getConfirmedBalance().subtract(transferFee).compareTo(BigInteger.ZERO) <= 0) {
            return "0";
        }
        return EthUtil.weiToEthString(this.balance.getConfirmedBalance().subtract(transferFee));
    }

    public String unconfirmedBalanceStringMinusTransferFee() {
        if (this.balance.getUnconfirmedBalance() == null || this.balance.getUnconfirmedBalance().subtract(transferFee).compareTo(BigInteger.ZERO) <= 0) {
            return "0";
        }
        return EthUtil.weiToEthString(this.balance.getUnconfirmedBalance().subtract(transferFee));
    }

    public BigInteger getUnconfirmedBalance() {
        return balance.getUnconfirmedBalance();
    }

    public BigDecimal getUnconfirmedBalanceAsEth() {
        return EthUtil.weiToEth(this.balance.getUnconfirmedBalance());
    }

    public String unconfirmedBalanceString() {
        if (this.balance.getUnconfirmedBalance() == null || this.balance.getUnconfirmedBalance().equals(BigInteger.ZERO)) {
            return "0";
        }
        return EthUtil.weiToEthString(this.balance.getUnconfirmedBalance());
    }

    public void setEthValue(final double eth_value){
        this.ethValue = eth_value;
    }

    public double getEthValue(){
        return this.ethValue;
    }

    // Get the value of ethereum in another currency
    public BigDecimal getMarketRate(final String currency, final BigDecimal ethAmount) {
        final BigDecimal rate = this.rates.getRate(currency);
        return rate.multiply(ethAmount);
    }
}