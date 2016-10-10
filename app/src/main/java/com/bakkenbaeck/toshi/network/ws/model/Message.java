package com.bakkenbaeck.toshi.network.ws.model;


import java.util.List;

public class Message {

    // private String recipient_id;
    // private String sender_id;
    private Internals payload;

    @Override
    public String toString() {
        return this.payload.message.text;
    }

    public List<Detail> getDetails(){
        return this.payload.message.details;
    }

    public String getType(){
        return this.payload.message.type;
    }

    private static class Internals {
        private MessageInternals message;
    }

    private static class MessageInternals {
        // private BigInteger rewards;
        // private BigInteger new_balance;
        private String text;
        private List<Detail> details;
        private String type;
    }
}
