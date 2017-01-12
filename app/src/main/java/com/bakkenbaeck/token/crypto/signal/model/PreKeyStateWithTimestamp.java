package com.bakkenbaeck.token.crypto.signal.model;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import org.whispersystems.libsignal.IdentityKey;
import org.whispersystems.signalservice.api.push.SignedPreKeyEntity;
import org.whispersystems.signalservice.internal.push.PreKeyEntity;
import org.whispersystems.signalservice.internal.util.JsonUtil;

import java.util.List;

@JsonPropertyOrder(alphabetic=true)
public class PreKeyStateWithTimestamp {
    @JsonProperty
    @JsonSerialize(using = JsonUtil.IdentityKeySerializer.class)
    @JsonDeserialize(using = JsonUtil.IdentityKeyDeserializer.class)
    private final IdentityKey identityKey;

    @JsonProperty
    private final List<PreKeyEntity> preKeys;

    @JsonProperty
    private final PreKeyEntity lastResortKey;

    @JsonProperty
    private final String password;

    @JsonProperty
    private final int registrationId;

    @JsonProperty
    private final String signalingKey;

    @JsonProperty
    private final SignedPreKeyEntity signedPreKey;

    @JsonProperty
    private final long timestamp;


    public PreKeyStateWithTimestamp(
            final List<PreKeyEntity> preKeys,
            final PreKeyEntity lastResortKey,
            final String password,
            final int registrationId,
            final String signalingKey,
            final SignedPreKeyEntity signedPreKey,
            final IdentityKey identityKey,
            final long timestamp) {
        this.preKeys = preKeys;
        this.lastResortKey = lastResortKey;
        this.password = password;
        this.registrationId = registrationId;
        this.signalingKey = signalingKey;
        this.signedPreKey = signedPreKey;
        this.identityKey = identityKey;
        this.timestamp = timestamp;
    }

}
