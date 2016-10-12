package com.bakkenbaeck.token.network.ws.model;


public class VerificationStart {

    private final String phoneNumber;

    public VerificationStart(final String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    @Override
    public String toString() {
        return "{" +
                " \"type\": \"verification_start\"," +
                " \"payload\": {" +
                "   \"phone_number\": \"" + this.phoneNumber + "\"" +
                " }" +
                "}";
    }
}
