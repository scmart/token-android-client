package com.bakkenbaeck.token.crypto.signal.model;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import org.whispersystems.libsignal.IdentityKey;
import org.whispersystems.signalservice.api.push.SignedPreKeyEntity;
import org.whispersystems.signalservice.internal.push.PreKeyEntity;
import org.whispersystems.signalservice.internal.util.JsonUtil;

import java.util.List;

public class PreKeyStateWithTimestamp {
    @JsonProperty
    @JsonSerialize(using = JsonUtil.IdentityKeySerializer.class)
    @JsonDeserialize(using = JsonUtil.IdentityKeyDeserializer.class)
    private IdentityKey identityKey;

    @JsonProperty
    private List<PreKeyEntity> preKeys;

    @JsonProperty
    private PreKeyEntity       lastResortKey;

    @JsonProperty
    private SignedPreKeyEntity signedPreKey;

    @JsonProperty
    private long timestamp;


    public PreKeyStateWithTimestamp(
            final List<PreKeyEntity> preKeys,
            final PreKeyEntity lastResortKey,
            final SignedPreKeyEntity signedPreKey,
            final IdentityKey identityKey,
            final long timestamp) {
        this.preKeys = preKeys;
        this.lastResortKey = lastResortKey;
        this.signedPreKey = signedPreKey;
        this.identityKey = identityKey;
        this.timestamp = timestamp;
    }

}
