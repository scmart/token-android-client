package com.bakkenbaeck.toshi.network.ws.model;

import io.realm.RealmObject;

public class Detail extends RealmObject{
    private String title;
    private double value;

    public String getTitle() {
        return title;
    }

    public double getValue() {
        return value;
    }
}
