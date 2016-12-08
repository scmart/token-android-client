package com.bakkenbaeck.token.crypto.signal.model;


import org.whispersystems.libsignal.IdentityKey;
import org.whispersystems.libsignal.InvalidKeyException;
import org.whispersystems.libsignal.SignalProtocolAddress;
import org.whispersystems.signalservice.internal.util.Base64;

import java.io.IOException;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class SignalIdentity extends RealmObject {

    @PrimaryKey
    private String id;
    private String name;
    private int deviceId;
    private String serializedIdentityKey;

    public SignalIdentity setIdentityKey(final IdentityKey identityKey) {
        this.serializedIdentityKey = Base64.encodeBytesWithoutPadding(identityKey.serialize());
        return this;
    }

    public SignalIdentity setSignalProtocolAddress(final SignalProtocolAddress address) {
        this.name = address.getName();
        this.deviceId = address.getDeviceId();
        this.id = this.name + String.valueOf(this.deviceId);
        return this;
    }

    public IdentityKey getIdentityKey() {
        try {
            return new IdentityKey(Base64.decodeWithoutPadding(this.serializedIdentityKey), 0);
        } catch (InvalidKeyException | IOException e) {
            return null;
        }
    }

    public SignalProtocolAddress getSignalProtocolAddress() {
        return new SignalProtocolAddress(this.name, this.deviceId);
    }

    public String getId() {
        return this.id;
    }
}
