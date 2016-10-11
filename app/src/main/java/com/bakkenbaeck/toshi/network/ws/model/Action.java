package com.bakkenbaeck.toshi.network.ws.model;

import io.realm.RealmObject;

public class Action extends RealmObject{
    private String title;
    private String action;

    public String getTitle() {
        return title;
    }

    public String getAction() {
        return action;
    }
}
