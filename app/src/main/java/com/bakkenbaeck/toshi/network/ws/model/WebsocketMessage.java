package com.bakkenbaeck.toshi.network.ws.model;


public class WebSocketMessage {
    public String type;

    @Override
    public String toString() {
        return "type: " + this.type;
    }
}
