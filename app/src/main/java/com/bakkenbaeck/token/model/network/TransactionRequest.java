package com.bakkenbaeck.token.model.network;


public class TransactionRequest {

    private String value;
    private String from;
    private String to;

    public TransactionRequest setValue(final String value) {
        this.value = value;
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
