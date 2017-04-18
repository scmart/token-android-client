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

package com.tokenbrowser.crypto.signal.store;

import com.tokenbrowser.crypto.signal.SignalPreferences;
import com.tokenbrowser.crypto.signal.model.SignalIdentity;

import org.whispersystems.libsignal.IdentityKey;
import org.whispersystems.libsignal.IdentityKeyPair;
import org.whispersystems.libsignal.InvalidKeyException;
import org.whispersystems.libsignal.SignalProtocolAddress;
import org.whispersystems.libsignal.state.IdentityKeyStore;

import io.realm.Realm;
import io.realm.RealmObject;

public class SignalIdentityKeyStore implements IdentityKeyStore {

    @Override
    public IdentityKeyPair getIdentityKeyPair() {
        try {
            final byte[] serializedKey = SignalPreferences.getSerializedIdentityKeyPair();
            if (serializedKey == null) {
                return null;
            }
            return new IdentityKeyPair(serializedKey);
        } catch (final InvalidKeyException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public int getLocalRegistrationId() {
        return SignalPreferences.getLocalRegistrationId();
    }

    @Override
    public void saveIdentity(final SignalProtocolAddress address, final IdentityKey identityKey) {
        final SignalIdentity identity =
            new SignalIdentity()
                .setSignalProtocolAddress(address)
                .setIdentityKey(identityKey);
        writeObjectToDatabase(identity);
    }

    @Override
    public boolean isTrustedIdentity(final SignalProtocolAddress address, final IdentityKey identityKey) {
        saveIdentity(address, identityKey);
        return true;
    }

    private void writeObjectToDatabase(final RealmObject object) {
        final Realm realm = Realm.getDefaultInstance();
        try {
            realm.beginTransaction();
            realm.copyToRealmOrUpdate(object);
            realm.commitTransaction();
        } finally {
            realm.close();
        }
    }
}