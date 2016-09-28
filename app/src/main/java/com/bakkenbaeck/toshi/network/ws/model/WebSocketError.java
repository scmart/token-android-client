package com.bakkenbaeck.toshi.network.ws.model;


import java.math.BigInteger;

public class WebSocketError {

    private Internals payload;

    public WebSocketErrors getCode() {
        return this.payload.code;
    }

    private static class Internals {

        private WebSocketErrors code;
    }
}
