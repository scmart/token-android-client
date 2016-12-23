package com.bakkenbaeck.token.crypto.signal.model;

public final class OutgoingMessage {

    private String body;
    private String address;
    private int id;

    public OutgoingMessage() {}

    public final OutgoingMessage setBody(final String body) {
        this.body = body;
        return this;
    }

    public final OutgoingMessage setAddress(final String address) {
        this.address = address;
        return this;
    }

    public final String getBody() {
        return body;
    }

    public final String getAddress() {
        return address;
    }

    // The ID can be used to identify a message
    // that failed to send
    public final OutgoingMessage setId(final int id) {
        this.id = id;
        return this;
    }
}
