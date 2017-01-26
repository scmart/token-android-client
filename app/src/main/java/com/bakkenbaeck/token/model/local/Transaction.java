package com.bakkenbaeck.token.model.local;


import java.math.BigDecimal;

public class Transaction {

    private final BigDecimal ethAmount;
    private final String toAddress;

    public Transaction(
            final BigDecimal ethAmount,
            final String toAddress) {
        this.ethAmount = ethAmount;
        this.toAddress = toAddress;
    }

    public BigDecimal getEthAmount() {
        return ethAmount;
    }

    public String getToAddress() {
        return toAddress;
    }
}
