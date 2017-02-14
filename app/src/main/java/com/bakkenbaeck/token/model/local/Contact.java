package com.bakkenbaeck.token.model.local;


import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Contact extends RealmObject {

    @PrimaryKey
    private String owner_address;
    private User user;

    public Contact() {}

    public Contact(final User user) {
        this.user = user;
        this.owner_address = user.getOwnerAddress();
    }

    public String getOwnerAddress() {
        return owner_address;
    }

    public User getUser() {
        return user;
    }
}
