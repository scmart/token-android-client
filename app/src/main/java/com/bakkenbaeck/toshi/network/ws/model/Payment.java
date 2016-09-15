package com.bakkenbaeck.toshi.network.ws.model;


import java.math.BigInteger;

public class Payment {

    private PaymentInternals payload;

    public BigInteger getAmount() {
        return new BigInteger(this.payload.amount);
    }

    public BigInteger getUnconfirmedBalance() {
        return new BigInteger(this.payload.new_balance);
    }

    public BigInteger getConfirmedBalance() {
        return new BigInteger(this.payload.confirmed_balance);
    }

    private static class PaymentInternals {

        private String new_balance;
        private String confirmed_balance;
        private String amount;
    }
}
