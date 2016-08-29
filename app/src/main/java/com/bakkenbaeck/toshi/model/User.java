package com.bakkenbaeck.toshi.model;


import java.math.BigInteger;

public class User {
    private String id;
    private BigInteger balance;

    public String getId() {
        return id;
    }

    public User setId(final String id) {
        this.id = id;
        return this;
    }

    public BigInteger getBalance() {
        return this.balance;
    }
}
