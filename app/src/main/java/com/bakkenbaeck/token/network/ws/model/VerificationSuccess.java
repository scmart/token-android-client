package com.bakkenbaeck.token.network.ws.model;

public class VerificationSuccess {
    private MessageInternals payload;

    private static class MessageInternals {
        private int reputation_gain;
        private int reputation;
        private String message;
        private String type;
    }

    public int getReputationGained(){
        return payload.reputation_gain;
    }
}
