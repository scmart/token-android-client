package com.bakkenbaeck.token.crypto.signal.model;


public class DecryptedSignalMessage {

    private final String body;
    private final String source;

    public DecryptedSignalMessage(final String source, final String body) {
        this.source = source;
        this.body = body;
    }

    public String getBody() {
        return body;
    }

    public String getSource() {
        return source;
    }
}
