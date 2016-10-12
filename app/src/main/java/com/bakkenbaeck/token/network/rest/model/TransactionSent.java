package com.bakkenbaeck.token.network.rest.model;


import java.math.BigInteger;

public class TransactionSent {

    private TransactionSentInternals payload;

    public BigInteger getAmount() {
        return this.payload.amount;
    }

    public BigInteger getUnconfirmedBalance() {
        return this.payload.new_balance;
    }

    public BigInteger getConfirmedBalance() {
        return this.payload.confirmed_balance;
    }

    private static class TransactionSentInternals {
        private BigInteger new_balance;
        private BigInteger amount;
        private BigInteger confirmed_balance;
    }
}
