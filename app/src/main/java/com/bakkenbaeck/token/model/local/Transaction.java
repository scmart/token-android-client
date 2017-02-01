package com.bakkenbaeck.token.model.local;


import java.math.BigDecimal;

public class Transaction {

    private String txHash;
    private BigDecimal ethAmount;
    private String toAddress;
    private String ownerAddress;

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

    public String getTxHash() {
        return this.txHash;
    }

    public void setTxHash(final String txHash) {
        this.txHash = txHash;
    }
}
