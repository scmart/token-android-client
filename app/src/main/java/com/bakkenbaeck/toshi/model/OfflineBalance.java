package com.bakkenbaeck.toshi.model;


import java.math.BigInteger;

public class OfflineBalance {

    private BigInteger amountInWei;
    private boolean hasWithdrawn = false;
    private int numberOfRewards = 0;

    public OfflineBalance() {
        this.amountInWei = BigInteger.ZERO;
    }

    public BigInteger getBalance() {
        return this.amountInWei;
    }

    public void setBalance(final BigInteger balance) {
        this.amountInWei = balance;
    }

    public void subtract(final BigInteger amount) {
        this.amountInWei = this.amountInWei.subtract(amount);
        this.hasWithdrawn = true;
    }

    public boolean hasWithdraw() {
        return this.hasWithdrawn;
    }

    public int getNumberOfRewards() {
        return this.numberOfRewards;
    }

    @Override
    public String toString() {
        if (this.amountInWei == null) {
            return "0";
        } else {
            return this.amountInWei.toString();
        }
    }
}
