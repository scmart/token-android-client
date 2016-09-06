package com.bakkenbaeck.toshi.model;


import java.math.BigInteger;

public class User {
    private String id;
    private BigInteger balance;
    private String auth_token;
    private String bcrypt_salt;

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

    public String getAuthToken() {
        return auth_token;
    }

    public String getBcryptSalt() {
        return bcrypt_salt;
    }
}
