package com.bakkenbaeck.token.model.local;


import java.math.BigDecimal;

public class Transaction {

    private final BigDecimal ethAmount;
    private final String toAddress;
    private final String ownerAddress;

    public Transaction(
            final BigDecimal ethAmount,
            final String toAddress,
            final String ownerAddress) {
        this.ethAmount = ethAmount;
        this.toAddress = toAddress;
        this.ownerAddress = ownerAddress;
    }

    public BigDecimal getEthAmount() {
        return ethAmount;
    }

    public String getToAddress() {
        return toAddress;
    }

    public String getOwnerAddress() {
        return this.ownerAddress;
    }
}
