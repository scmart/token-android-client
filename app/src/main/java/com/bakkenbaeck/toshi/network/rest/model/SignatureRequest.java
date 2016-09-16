package com.bakkenbaeck.toshi.network.rest.model;


public class SignatureRequest {

    private SignatureRequestInternals payload;

    public String getTransaction() {
        return this.payload.transaction;
    }

    private static class SignatureRequestInternals {
        private String transaction;
    }
}
