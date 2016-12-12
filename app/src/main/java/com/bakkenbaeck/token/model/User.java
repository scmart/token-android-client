package com.bakkenbaeck.token.model;


import java.math.BigInteger;

public class User {

    private String owner_address;
    private String username;

    public String getUsername() {
        return username;
    }

    public User setUsername(final String username) {
        this.username = username;
        return this;
    }

    public String getOwnerAddress() {
        return owner_address;
    }

    public User setOwnerAddress(final String ownerAddress) {
        this.owner_address = ownerAddress;
        return this;
    }

    public BigInteger getUnconfirmedBalance() {
        // Todo
        return BigInteger.ZERO;
    }

    public BigInteger getConfirmedBalance() {
        // Todo
        return BigInteger.ZERO;
    }

    public int getLevel() {
        // Todo
        return 1;
    }

    public double getEthValue() {
        // Todo
        return 1;
    }
}
