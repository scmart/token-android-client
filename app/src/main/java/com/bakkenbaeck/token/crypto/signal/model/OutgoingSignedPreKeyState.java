package com.bakkenbaeck.token.crypto.signal.model;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder(alphabetic=true)
public class OutgoingSignedPreKeyState {
    @JsonProperty
    private PreKeyStateWithTimestamp payload;

    @JsonProperty
    private String address;

    @JsonProperty
    private String signature;


    public OutgoingSignedPreKeyState(
            final PreKeyStateWithTimestamp payload,
            final String signature,
            final String address) {
        this.payload = payload;
        this.signature = signature;
        this.address = address;
    }

}
