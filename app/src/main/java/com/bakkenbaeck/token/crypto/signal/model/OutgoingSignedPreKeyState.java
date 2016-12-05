package com.bakkenbaeck.token.crypto.signal.model;


import com.fasterxml.jackson.annotation.JsonProperty;

public class OutgoingSignedPreKeyState {
    @JsonProperty
    private PreKeyStateWithTimestamp payload;

    @JsonProperty
    private String signature;


    public OutgoingSignedPreKeyState(
            final PreKeyStateWithTimestamp payload,
            final String signature) {
        this.payload = payload;
        this.signature = signature;
    }

}
