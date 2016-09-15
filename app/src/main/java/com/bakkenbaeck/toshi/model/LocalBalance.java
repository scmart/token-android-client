package com.bakkenbaeck.toshi.model;


import java.math.BigInteger;

// Represents both confirmed and unconfirmed balances in wei for the device.
// It may be out of sync with reality.
public class LocalBalance {

    private BigInteger confirmedBalance = BigInteger.ZERO;
    private BigInteger unconfirmedBalance = BigInteger.ZERO;

    public BigInteger getConfirmedBalance() {
        return confirmedBalance;
    }

    public void setConfirmedBalance(final BigInteger confirmedBalance) {
        this.confirmedBalance = confirmedBalance;
    }

    public BigInteger getUnconfirmedBalance() {
        return unconfirmedBalance;
    }

    public void setUnconfirmedBalance(final BigInteger unconfirmedBalance) {
        this.unconfirmedBalance = unconfirmedBalance;
    }

    public String unconfirmedBalanceString() {
        if (this.unconfirmedBalance == null) {
            return "0";
        }
        return this.unconfirmedBalance.toString();
    }
}
