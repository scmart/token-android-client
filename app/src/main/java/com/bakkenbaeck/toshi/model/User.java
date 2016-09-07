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
        return this.auth_token;
    }

    public String getBcryptSalt() {
        return this.bcrypt_salt;
    }

    public void setAuthToken(final String auth_token) {
        this.auth_token = auth_token;
    }

    public void setBcryptSalt(final String bcrypt_salt) {
        this.bcrypt_salt = bcrypt_salt;
    }

    public boolean isNewUser() {
        // Auth token is only returned when we first create a user.
        return this.auth_token != null;
    }
}
