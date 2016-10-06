package com.bakkenbaeck.toshi.network.ws.model;


public class Message {

    // private String recipient_id;
    // private String sender_id;
    private Internals payload;

    @Override
    public String toString() {
        return this.payload.message.text;
    }

    private static class Internals {
        private MessageInternals message;
    }

    private static class MessageInternals {
        // private BigInteger rewards;
        // private BigInteger new_balance;
        private String text;
        // private String type;
    }
}
