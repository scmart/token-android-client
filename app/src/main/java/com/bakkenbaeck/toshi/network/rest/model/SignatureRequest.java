package com.bakkenbaeck.toshi.network.rest.model;


public class SignatureRequest {

    private SignatureRequestInternals payload;

    public String getTransaction() {
        return this.payload.transaction;
    }

    public String getMessage(){
        return payload.message;
    }

    private static class SignatureRequestInternals {
        private String transaction;
        private String message;
        private String code;
        private String type;
    }
}
