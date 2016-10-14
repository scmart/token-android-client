package com.bakkenbaeck.token.network.ws.model;

public class VerificationSuccess {
    private MessageInternals payload;

    private static class MessageInternals {
        private int experience_gain;
        private int level;
        private String message;
        private String type;
    }

    public int getReputationGained(){
        return payload.experience_gain;
    }
}
