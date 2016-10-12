package com.bakkenbaeck.token.network.ws.model;


import java.util.List;

public class Message {

    // private String recipient_id;
    // private String sender_id;
    private MessageInternals payload;

    private static class Internals {
        private MessageInternals message;
    }

    private static class MessageInternals {
        // private BigInteger rewards;
        // private BigInteger new_balance;
        private String text;
        private List<Detail> details;
        private List<Action> actions;
        private String type;
    }

    @Override
    public String toString() {
        return this.payload.text;
    }

    public List<Detail> getDetails(){
        return this.payload.details;
    }

    public String getType(){
        return this.payload.type;
    }

    public List<Action> getActions(){
        return payload.actions;
    }
}
