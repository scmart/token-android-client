/*
 * 	Copyright (c) 2017. Token Browser, Inc
 *
 * 	This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.tokenbrowser.crypto.signal.model;


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
