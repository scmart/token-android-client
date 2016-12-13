package com.bakkenbaeck.token.manager;


import com.bakkenbaeck.token.crypto.HDWallet;
import com.bakkenbaeck.token.network.rest.BalanceService;
import com.bakkenbaeck.token.network.rest.model.Balance;
import com.bakkenbaeck.token.util.EthUtil;
import com.bakkenbaeck.token.util.SingleSuccessSubscriber;

import java.math.BigDecimal;
import java.math.BigInteger;

import rx.schedulers.Schedulers;

public class BalanceManager {

    private HDWallet wallet;
    private Balance balance;

    public BalanceManager init(final HDWallet wallet) {
        this.wallet = wallet;
        this.balance = new Balance();
        syncBalanceWithServer(wallet);
        return this;
    }

    private void syncBalanceWithServer(final HDWallet wallet) {
        BalanceService.getApi()
                .getBalance(wallet.getAddress())
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
}