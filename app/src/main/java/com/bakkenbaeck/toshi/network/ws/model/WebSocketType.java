package com.bakkenbaeck.toshi.network.ws.model;


public class WebSocketType {
    private String type;

    public String get() {
        return this.type;
    }

    @Override
    public String toString() {
        return "type: " + this.type;
    }
}
