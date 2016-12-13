package com.bakkenbaeck.token.network.rest.model;


import java.math.BigInteger;

public class TransactionRequest {

    private BigInteger amount;
    private String from;
    private String to;

    public TransactionRequest setAmount(final BigInteger amountInWei) {
        this.amount = amountInWei;
        return this;
    }

    public TransactionRequest setToAddress(final String addressInHex) {
        this.to = addressInHex;
        return this;
    }

    public TransactionRequest setFromAddress(final String addressInHex) {
        this.from = addressInHex;
        return this;
    }
}
