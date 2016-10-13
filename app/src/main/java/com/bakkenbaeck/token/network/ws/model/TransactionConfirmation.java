package com.bakkenbaeck.token.network.ws.model;


import java.math.BigInteger;

public class TransactionConfirmation {

    private TransactionInternals payload;

    public TransactionConfirmation(String balance, String confirmedBalance){
        if(payload == null){
            payload = new TransactionInternals();
        }
        payload.confirmed_balance = confirmedBalance;
        payload.balance = balance;
    }

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
