package com.bakkenbaeck.token.crypto.signal.store;


import com.bakkenbaeck.token.crypto.signal.SignalPreferences;
import com.bakkenbaeck.token.crypto.signal.util.PreKeyUtil;
import com.bakkenbaeck.token.crypto.util.HashUtil;
import com.bakkenbaeck.token.view.BaseApplication;

import org.whispersystems.libsignal.IdentityKey;
import org.whispersystems.libsignal.IdentityKeyPair;
import org.whispersystems.libsignal.InvalidKeyException;
import org.whispersystems.libsignal.InvalidKeyIdException;
import org.whispersystems.libsignal.SignalProtocolAddress;
import org.whispersystems.libsignal.state.IdentityKeyStore;
import org.whispersystems.libsignal.state.PreKeyRecord;
import org.whispersystems.libsignal.state.PreKeyStore;
import org.whispersystems.libsignal.state.SessionRecord;
import org.whispersystems.libsignal.state.SessionStore;
import org.whispersystems.libsignal.state.SignalProtocolStore;
import org.whispersystems.libsignal.state.SignedPreKeyRecord;
import org.whispersystems.libsignal.state.SignedPreKeyStore;
import org.whispersystems.libsignal.util.KeyHelper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ProtocolStore implements SignalProtocolStore {

    private static final int PREKEY_START = 1234;
    private static final int PREKEY_COUNT = 100;
    private static final int SIGNED_PREKEY_ID = 1;

    private final PreKeyStore preKeyStore;
    private final SignedPreKeyStore signedPreKeyStore;
    private final IdentityKeyStore identityKeyStore;
    private final SessionStore sessionStore;

    public ProtocolStore() {
        this.preKeyStore  = new SignalPreKeyStore();
        this.signedPreKeyStore = new SignalPreKeyStore();
        this.identityKeyStore = new SignalIdentityKeyStore();
        this.sessionStore = new SignalSessionStore();
    }

    public ProtocolStore init() {
        try {
            if (getIdentityKeyPair() == null) generateIdentityKeyPair();
            if (getLastResortKey() == null) generateLastResortKey();
            if (getPassword() == null) generatePassword();
            if (getLocalRegistrationId() == -1) generateLocalRegistrationId();
            if (getSignalingKey() == null) generateSignalingKey();
            if (getSignedPreKey() == null) generateSignedPreKey();
            if (!preKeysHaveBeenGenerated()) generatePreKeys();
        } catch (final IOException | InvalidKeyIdException | InvalidKeyException ex) {
            throw new RuntimeException(ex);
        }
        return this;
    }

    public SignedPreKeyRecord getSignedPreKey() throws InvalidKeyIdException {
        final int signedPreKeyId = SignalPreferences.getSignedPreKeyId();
        if (signedPreKeyId == -1) return null;
        return loadSignedPreKey(signedPreKeyId);
    }

    private void generateSignedPreKey() throws InvalidKeyException {
        final SignedPreKeyRecord pk = PreKeyUtil.generateSignedPreKey(BaseApplication.get(), getIdentityKeyPair(), true);
        storeSignedPreKey(pk.getId(), pk);
        SignalPreferences.setSignedPreKeyId(pk.getId());
    }

    @Override
    public IdentityKeyPair getIdentityKeyPair() {
        return identityKeyStore.getIdentityKeyPair();
    }

    private void generateIdentityKeyPair() {
        SignalPreferences.setSerializedIdentityKeyPair(KeyHelper.generateIdentityKeyPair().serialize());
    }

    @Override
    public int getLocalRegistrationId() {
        return identityKeyStore.getLocalRegistrationId();
    }

    private void generateLocalRegistrationId() {
        SignalPreferences.setLocalRegistrationId(KeyHelper.generateRegistrationId(false));
    }

    public String getSignalingKey() {
        return SignalPreferences.getSignalingKey();
    }

    private void generateSignalingKey() {
        SignalPreferences.setSignalingKey(HashUtil.getSecret(52));
    }

    @Override
    public void saveIdentity(SignalProtocolAddress address, IdentityKey identityKey) {
        identityKeyStore.saveIdentity(address, identityKey);
    }

    @Override
    public boolean isTrustedIdentity(SignalProtocolAddress address, IdentityKey identityKey) {
        return identityKeyStore.isTrustedIdentity(address, identityKey);
    }

    private boolean preKeysHaveBeenGenerated() {
        return this.preKeyStore.containsPreKey(PREKEY_START) && this.preKeyStore.containsPreKey(PREKEY_START + PREKEY_COUNT - 1);
    }

    private void generatePreKeys() {
        final List<PreKeyRecord> preKeyRecords = PreKeyUtil.generatePreKeys(BaseApplication.get());
        for (final PreKeyRecord preKeyRecord : preKeyRecords) {
            storePreKey(preKeyRecord.getId(), preKeyRecord);
        }
    }

    public List<PreKeyRecord> getPreKeys() throws InvalidKeyIdException {
        final List<PreKeyRecord> preKeyRecords = new ArrayList<>(PREKEY_COUNT);
        for (int i = PREKEY_START; i < PREKEY_START + PREKEY_COUNT; i++) {
            final PreKeyRecord preKeyRecord = loadPreKey(i);
            preKeyRecords.add(preKeyRecord);
        }
        return preKeyRecords;
    }

    public PreKeyRecord getLastResortKey() throws IOException {
        final byte[] serializedLastResortKey = SignalPreferences.getSerializedLastResortKey();
        if (serializedLastResortKey == null) return null;
        return new PreKeyRecord(serializedLastResortKey);
    }

    private void generateLastResortKey() {
        SignalPreferences.setSerializedLastResortKey(PreKeyUtil.generateLastResortKey(BaseApplication.get()).serialize());
    }

    public String getPassword() {
        return SignalPreferences.getPassword();
    }

    private void generatePassword() {
        SignalPreferences.setPassword(HashUtil.getSecret(18));
    }

    @Override
    public PreKeyRecord loadPreKey(int preKeyId) throws InvalidKeyIdException {
        return preKeyStore.loadPreKey(preKeyId);
    }

    @Override
    public void storePreKey(int preKeyId, PreKeyRecord record) {
        preKeyStore.storePreKey(preKeyId, record);
    }

    @Override
    public boolean containsPreKey(int preKeyId) {
        return preKeyStore.containsPreKey(preKeyId);
    }

    @Override
    public void removePreKey(int preKeyId) {
        preKeyStore.removePreKey(preKeyId);
    }

    @Override
    public SessionRecord loadSession(SignalProtocolAddress axolotlAddress) {
        return sessionStore.loadSession(axolotlAddress);
    }

    @Override
    public List<Integer> getSubDeviceSessions(String number) {
        return sessionStore.getSubDeviceSessions(number);
    }

    @Override
    public void storeSession(SignalProtocolAddress axolotlAddress, SessionRecord record) {
        sessionStore.storeSession(axolotlAddress, record);
    }

    @Override
    public boolean containsSession(SignalProtocolAddress axolotlAddress) {
        return sessionStore.containsSession(axolotlAddress);
    }

    @Override
    public void deleteSession(SignalProtocolAddress axolotlAddress) {
        sessionStore.deleteSession(axolotlAddress);
    }

    @Override
    public void deleteAllSessions(String number) {
        sessionStore.deleteAllSessions(number);
    }

    @Override
    public SignedPreKeyRecord loadSignedPreKey(int signedPreKeyId) throws InvalidKeyIdException {
        return signedPreKeyStore.loadSignedPreKey(signedPreKeyId);
    }

    @Override
    public List<SignedPreKeyRecord> loadSignedPreKeys() {
        return signedPreKeyStore.loadSignedPreKeys();
    }

    @Override
    public void storeSignedPreKey(int signedPreKeyId, SignedPreKeyRecord record) {
        signedPreKeyStore.storeSignedPreKey(signedPreKeyId, record);
    }

    @Override
    public boolean containsSignedPreKey(int signedPreKeyId) {
        return signedPreKeyStore.containsSignedPreKey(signedPreKeyId);
    }

    @Override
    public void removeSignedPreKey(int signedPreKeyId) {
        signedPreKeyStore.removeSignedPreKey(signedPreKeyId);
    }
}
