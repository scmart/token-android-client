package com.bakkenbaeck.token.network.ws.model;


public class WebSocketType {
    private String type;
    private String sender_id;

    public String get() {
        return this.type;
    }

    public String getSenderId(){
        return sender_id;
    }

    @Override
    public String toString() {
        return "type: " + this.type;
    }
}
