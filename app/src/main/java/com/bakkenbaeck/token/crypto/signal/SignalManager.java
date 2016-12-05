package com.bakkenbaeck.token.crypto.signal;


import com.bakkenbaeck.token.R;
import com.bakkenbaeck.token.crypto.HDWallet;
import com.bakkenbaeck.token.crypto.signal.store.SignalIdentityKeyStore;
import com.bakkenbaeck.token.crypto.signal.store.SignalPreKeyStore;
import com.bakkenbaeck.token.crypto.signal.store.SignalSessionStore;
import com.bakkenbaeck.token.crypto.util.HashUtil;
import com.bakkenbaeck.token.view.BaseApplication;

import org.whispersystems.libsignal.IdentityKeyPair;
import org.whispersystems.libsignal.InvalidKeyException;
import org.whispersystems.libsignal.InvalidKeyIdException;
import org.whispersystems.libsignal.state.PreKeyRecord;
import org.whispersystems.libsignal.state.SignedPreKeyRecord;
import org.whispersystems.libsignal.util.KeyHelper;
import org.whispersystems.signalservice.api.push.TrustStore;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class SignalManager {

    private static final int PREKEY_START = 1234;
    private static final int PREKEY_COUNT = 100;
    private static final int SIGNED_PREKEY_ID = 1;

    private IdentityKeyPair identityKeyPair;
    private int registrationId;
    private String password;
    private String signalingKey;
    private List<PreKeyRecord> preKeys;
    private SignedPreKeyRecord signedPreKey;
    private PreKeyRecord lastResortKey;
    private SignalPreKeyStore preKeyStore;
    private SignalSessionStore sessionStore;
    private SignalIdentityKeyStore identityStore;
    private SignalAccountManager accountManager;
    private HDWallet wallet;

    public SignalManager init(final HDWallet wallet) {
        this.wallet = wallet;
        new Thread(new Runnable() {
            @Override
            public void run() {
                initSignalManager();
            }
        }).start();

        return this;
    }

    private void initSignalManager() {
        generateStores();
        loadOrGenerateKeys();
    }

    private void generateStores() {
        this.preKeyStore = new SignalPreKeyStore();
        this.sessionStore = new SignalSessionStore();
        this.identityStore = new SignalIdentityKeyStore();
        final TrustStore trustStore = new TrustStore() {
            @Override
            public InputStream getKeyStoreInputStream() {
                return BaseApplication.get().getResources().openRawResource(R.raw.heroku);
            }

            @Override
            public String getKeyStorePassword() {
                return "whisper";
            }
        };
        this.accountManager = new SignalAccountManager(trustStore, this.wallet);
    }

    private void loadOrGenerateKeys() {
        try {
            if (keysAlreadyCreated()) {
                loadKeys();
            } else {
                generateKeys();
            }

            if (!haveRegisteredWithServer()) {
                registerWithServer();
            }
        } catch (final InvalidKeyException | InvalidKeyIdException | IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private void loadKeys() throws InvalidKeyException, InvalidKeyIdException, IOException {
        this.registrationId = SignalPreferences.getLocalRegistrationId();
        this.signalingKey = SignalPreferences.getSignalingKey();
        this.password = SignalPreferences.getPassword();

        final byte[] serializedKey = SignalPreferences.getSerializedIdentityKeyPair();
        this.identityKeyPair = new IdentityKeyPair(serializedKey);

        final byte[] serializedLastResortKey = SignalPreferences.getSerializedLastResortKey();
        this.lastResortKey = new PreKeyRecord(serializedLastResortKey);

        this.preKeys = new ArrayList<>(PREKEY_COUNT);
        for (int i = PREKEY_START; i < PREKEY_START + PREKEY_COUNT; i++) {
            final PreKeyRecord preKeyRecord = this.preKeyStore.loadPreKey(i);
            this.preKeys.add(preKeyRecord);
        }

        final int signedPreKeyId = SignalPreferences.getSignedPreKeyId();
        this.signedPreKey = this.preKeyStore.loadSignedPreKey(signedPreKeyId);
    }

    private void generateKeys() throws InvalidKeyException {
        this.identityKeyPair = KeyHelper.generateIdentityKeyPair();
        this.lastResortKey = KeyHelper.generateLastResortPreKey();
        this.preKeys = KeyHelper.generatePreKeys(PREKEY_START, PREKEY_COUNT);
        this.registrationId = KeyHelper.generateRegistrationId(false);
        this.signedPreKey = KeyHelper.generateSignedPreKey(identityKeyPair, SIGNED_PREKEY_ID);
        this.signalingKey = HashUtil.getSecret(52);
        this.password = HashUtil.getSecret(18);

        storeGeneratedKeys();
        saveToPreferences();
    }

    private void storeGeneratedKeys() {
        this.preKeyStore.storeSignedPreKey(this.signedPreKey.getId(), this.signedPreKey);
        for (final PreKeyRecord preKeyRecord : this.preKeys) {
            this.preKeyStore.storePreKey(preKeyRecord.getId(), preKeyRecord);
        }
    }

    private void saveToPreferences() {
        SignalPreferences.setSerializedIdentityKeyPair(this.identityKeyPair.serialize());
        SignalPreferences.setLocalRegistrationId(this.registrationId);
        SignalPreferences.setSerializedLastResortKey(this.lastResortKey.serialize());
        SignalPreferences.setSignalingKey(this.signalingKey);
        SignalPreferences.setSignedPreKeyId(this.signedPreKey.getId());
        SignalPreferences.setPassword(this.password);
    }

    private void registerWithServer() {
        try {
            this.accountManager.registerKeys(
                    this.identityKeyPair.getPublicKey(),
                    this.lastResortKey,
                    this.password,
                    this.registrationId,
                    this.signalingKey,
                    this.signedPreKey,
                    this.preKeys);
            SignalPreferences.setRegisteredWithServer();
        } catch (final IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private boolean keysAlreadyCreated() {
        return SignalPreferences.getLocalRegistrationId() != -1;
    }

    private boolean haveRegisteredWithServer() {
        return SignalPreferences.getRegisteredWithServer();
    }
}
