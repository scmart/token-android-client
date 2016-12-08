package com.bakkenbaeck.token.crypto.signal.store;

import com.bakkenbaeck.token.crypto.signal.SignalPreferences;
import com.bakkenbaeck.token.crypto.signal.model.SignalIdentity;

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
        realm.beginTransaction();
        realm.copyToRealmOrUpdate(object);
        realm.commitTransaction();
    }
}