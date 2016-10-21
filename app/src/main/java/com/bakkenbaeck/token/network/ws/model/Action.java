package com.bakkenbaeck.token.network.ws.model;

import io.realm.RealmObject;

public class Action extends RealmObject{
    private long reset_time;
    private String title;
    private String action;

    public String getTitle() {
        return title;
    }

    public String getAction() {
        return action;
    }

    public long getReset_time(){
        return reset_time;
    }
}
