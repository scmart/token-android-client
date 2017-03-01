package com.tokenbrowser.model.local;


import io.realm.RealmObject;

public class CustomUserInformation extends RealmObject {
    private String about;
    private String avatar;
    private String location;
    private String name;

    public CustomUserInformation() {}

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
}