package com.bakkenbaeck.token.crypto.signal;

import android.content.Context;

import com.bakkenbaeck.token.util.LogUtil;
import com.bakkenbaeck.token.view.BaseApplication;

import org.whispersystems.libsignal.IdentityKey;
import org.whispersystems.libsignal.IdentityKeyPair;
import org.whispersystems.libsignal.InvalidKeyException;
import org.whispersystems.libsignal.SignalProtocolAddress;
import org.whispersystems.libsignal.state.IdentityKeyStore;

public class SignalIdentityKeyStore implements IdentityKeyStore {

    private final Context context;

    public SignalIdentityKeyStore() {
        this.context = BaseApplication.get();
    }

    @Override
    public IdentityKeyPair getIdentityKeyPair() {
        try {
            final byte[] serializedKey = SignalPreferences.getSerializedIdentityKeyPair();
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
    public void saveIdentity(SignalProtocolAddress address, IdentityKey identityKey) {
        // Todo
        LogUtil.print(getClass(), "ToDo");
    }

    @Override
    public boolean isTrustedIdentity(SignalProtocolAddress address, IdentityKey identityKey) {
        // Todo
        return true;
    }
}