package com.bakkenbaeck.token.network.rest.model;


public class SignatureRequest {

    private SignatureRequestInternals payload;
    private String type;

    public String getType(){
        return type;
    }

    public String getTransaction() {
        return this.payload.transaction;
    }

    public String getMessage(){
        return payload.message;
    }

    public String getCode(){
        return payload.code;
    }

    private static class SignatureRequestInternals {
        private String transaction;
        private String message;
        private String code;
        private String type;
    }
}
