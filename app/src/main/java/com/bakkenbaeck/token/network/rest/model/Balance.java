package com.bakkenbaeck.token.network.rest.model;


import java.math.BigInteger;

public class Balance {

    private BigInteger confirmed_balance = BigInteger.ZERO;
    private BigInteger unconfirmed_balance  = BigInteger.ZERO;

    public BigInteger getConfirmedBalance() {
        return confirmed_balance;
    }

    public BigInteger getUnconfirmedBalance() {
        return unconfirmed_balance;
    }
}
