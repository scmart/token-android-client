package com.bakkenbaeck.toshi.network.ws.model;

public class WebSocketError {

    private Internals payload;

    public WebSocketErrors getCode() {
        return this.payload.code;
    }

    private static class Internals {

        private WebSocketErrors code;
    }
}
