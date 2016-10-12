package com.bakkenbaeck.toshi.network.ws.model;

import io.realm.RealmObject;

public class Detail extends RealmObject{
    private String title;
    private double value;

    public Detail(String title, int value){
        this.title = title;
        this.value = value;
    }

    public Detail(){

    }

    public String getTitle() {
        return title;
    }

    public double getValue() {
        return value;
    }
}
