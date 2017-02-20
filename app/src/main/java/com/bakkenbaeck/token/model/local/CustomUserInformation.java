package com.bakkenbaeck.token.model.local;


import io.realm.RealmObject;

public class CustomUserInformation extends RealmObject {
    private String about;
    private String avatar;
    private String location;
    private String name;
    private String payment_address;

    public CustomUserInformation() {}

    /* package */ CustomUserInformation(final CustomUserInformation customUserInformation) {
        this.about = customUserInformation.getAbout();
        this.avatar = customUserInformation.getAvatar();
        this.location = customUserInformation.getLocation();
        this.name = customUserInformation.getName();
        this.payment_address = customUserInformation.getPaymentAddress();
    }

    public String getAbout() {
        return this.about;
    }

    public String getAvatar() {
        return this.avatar;
    }

    public String getLocation() {
        return this.location;
    }

    public String getName() {
        return this.name;
    }

    public String getPaymentAddress() {
        return this.payment_address;
    }
}