package com.bakkenbaeck.toshi.model;


import com.bakkenbaeck.toshi.util.EthUtil;

import java.math.BigDecimal;
import java.math.BigInteger;

// Represents both confirmed and unconfirmed balances in wei for the device.
// It may be out of sync with reality.
public class LocalBalance {

    // Both balances are stored in wei
    private BigInteger confirmedBalance = BigInteger.ZERO;
    private BigInteger unconfirmedBalance = BigInteger.ZERO;


    public BigInteger getConfirmedBalance() {
        return confirmedBalance;
    }

    public void setConfirmedBalance(final BigInteger confirmedBalance) {
        this.confirmedBalance = confirmedBalance;
    }

    public BigDecimal getConfirmedBalanceAsEth() {
        return EthUtil.weiToEth(this.confirmedBalance);
    }

    public String confirmedBalanceString() {
        if (this.confirmedBalance == null || this.confirmedBalance.equals(BigInteger.ZERO)) {
            return "0.0000000000";
        }
        return EthUtil.weiToEthString(this.confirmedBalance);
    }

    public void setUnconfirmedBalance(final BigInteger unconfirmedBalance) {
        this.unconfirmedBalance = unconfirmedBalance;
    }

    public BigInteger getUnconfirmedBalance() {
        return unconfirmedBalance;
    }

    public BigDecimal getUnconfirmedBalanceAsEth() {
        return EthUtil.weiToEth(this.unconfirmedBalance);
    }

    public String unconfirmedBalanceString() {
        if (this.unconfirmedBalance == null || this.unconfirmedBalance.equals(BigInteger.ZERO)) {
            return "0.0000000000";
        }
        return EthUtil.weiToEthString(this.unconfirmedBalance);
    }

}
