package com.bakkenbaeck.token.model;


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

    public int getLevel() {
        // Todo
        return 1;
    }
}
