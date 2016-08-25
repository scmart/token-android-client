package com.bakkenbaeck.toshi.model;


public class User {
    private String id;
    private int balance;

    public String getId() {
        return id;
    }

    public User setId(final String id) {
        this.id = id;
        return this;
    }

    public int getBalance() {
        return this.balance;
    }
}
