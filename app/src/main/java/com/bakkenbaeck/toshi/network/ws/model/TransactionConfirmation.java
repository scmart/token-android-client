package com.bakkenbaeck.toshi.network.ws.model;


import java.math.BigInteger;

public class TransactionConfirmation {

    private TransactionInternals payload;

    public BigInteger getUnconfirmedBalance() {
        return new BigInteger(this.payload.balance);
    }

    public BigInteger getConfirmedBalance() {
        return new BigInteger(this.payload.confirmed_balance);
    }

    private static class TransactionInternals {

        private String balance;
        private String confirmed_balance;
    }
}
