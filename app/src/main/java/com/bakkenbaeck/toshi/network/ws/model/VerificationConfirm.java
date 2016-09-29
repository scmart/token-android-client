package com.bakkenbaeck.toshi.network.ws.model;


public class VerificationConfirm {

    private final String phoneNumber;
    private final String verificationCode;

    public VerificationConfirm(final String phoneNumber, final String verificationCode) {
        this.phoneNumber = phoneNumber;
        this.verificationCode = verificationCode;
    }

    @Override
    public String toString() {
        return "{" +
                " \"type\": \"verification_confirm\"," +
                " \"payload\": {" +
                "   \"phone_number\": \"" + this.phoneNumber + "\"," +
                "   \"verification_code\": \"" + this.verificationCode + "\"" +
                " }" +
                "}";
    }
}
