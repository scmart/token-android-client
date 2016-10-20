package com.bakkenbaeck.token.network.ws.model;

public class VerificationSuccess {
    private MessageInternals payload;

    public int getLevel(){
        return payload.level;
    }

    private static class MessageInternals {
        private int experience_gain;
        private int level;
        private String message;
        private String type;
    }

    public String getMessage(){
        return payload.message;
    }
}
