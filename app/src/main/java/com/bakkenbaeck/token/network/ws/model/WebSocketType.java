package com.bakkenbaeck.token.network.ws.model;


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
