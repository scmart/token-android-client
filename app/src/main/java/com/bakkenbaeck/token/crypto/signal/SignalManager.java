package com.bakkenbaeck.token.crypto.signal;


import com.bakkenbaeck.token.BuildConfig;
import com.bakkenbaeck.token.R;
import com.bakkenbaeck.token.util.LogUtil;
import com.bakkenbaeck.token.view.BaseApplication;

import org.whispersystems.libsignal.DuplicateMessageException;
import org.whispersystems.libsignal.IdentityKeyPair;
import org.whispersystems.libsignal.InvalidKeyException;
import org.whispersystems.libsignal.InvalidKeyIdException;
import org.whispersystems.libsignal.InvalidMessageException;
import org.whispersystems.libsignal.InvalidVersionException;
import org.whispersystems.libsignal.LegacyMessageException;
import org.whispersystems.libsignal.SessionCipher;
import org.whispersystems.libsignal.SignalProtocolAddress;
import org.whispersystems.libsignal.UntrustedIdentityException;
import org.whispersystems.libsignal.protocol.PreKeySignalMessage;
import org.whispersystems.libsignal.state.PreKeyRecord;
import org.whispersystems.libsignal.state.SignedPreKeyRecord;
import org.whispersystems.libsignal.util.KeyHelper;
import org.whispersystems.signalservice.api.SignalServiceAccountManager;
import org.whispersystems.signalservice.api.push.TrustStore;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class SignalManager {

    private static final int PREKEY_START = 1234;
    private static final int PREKEY_COUNT = 100;
    private static final int SIGNED_PREKEY_ID = 1;

    private IdentityKeyPair identityKeyPair;
    private int registrationId;
    private List<PreKeyRecord> preKeys;
    private SignedPreKeyRecord signedPreKey;
    private SignalPreKeyStore preKeyStore;
    private SignalSessionStore sessionStore;
    private SignalIdentityKeyStore identityStore;
    private TrustStore trustStore;
    private SignalServiceAccountManager accountManager;

    public SignalManager init() {

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
        this.trustStore = new TrustStore() {
            @Override
            public InputStream getKeyStoreInputStream() {
                return BaseApplication.get().getResources().openRawResource(R.raw.whisper);
            }

            @Override
            public String getKeyStorePassword() {
                return "whisper";
            }
        };
        createAccount();
    }

    private void createAccount() {
        this.accountManager = new SignalServiceAccountManager(
                BaseApplication.get().getResources().getString(R.string.signal_url),
                this.trustStore,
                "unused",
                "unused",
                generateUserAgent());
    }

    private void loadOrGenerateKeys() {
        try {
            if (keysAlreadyCreated()) {
                loadKeys();
            } else {
                generateKeys();
            }
        } catch (final InvalidKeyException | InvalidKeyIdException ex) {
            throw new RuntimeException(ex);
        }
    }

    private String generateUserAgent() {
        return "Android " +
                BuildConfig.APPLICATION_ID +
                " - " +
                BuildConfig.VERSION_NAME +
                ":" +
                BuildConfig.VERSION_CODE;
    }
/*
    private void register() {
        try {
            accountManager.requestSmsVerificationCode();
            final String smsCode = "920144";
            accountManager.verifyAccountWithCode(smsCode, "signallingKey", 1, false);
            accountManager.setGcmId(Optional.of("gcm_id"));
            accountManager.setPreKeys(this.identityKeyPair.getPublicKey(), this.preKeys.get(0), this.signedPreKey, this.preKeys);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
*/
    private boolean keysAlreadyCreated() {
        return SignalPreferences.getLocalRegistrationId() != -1;
    }

    private void loadKeys() throws InvalidKeyException, InvalidKeyIdException {
        this.registrationId = SignalPreferences.getLocalRegistrationId();

        final byte[] serializedKey = SignalPreferences.getSerializedIdentityKeyPair();
        this.identityKeyPair = new IdentityKeyPair(serializedKey);

        this.preKeys = new ArrayList<>(PREKEY_COUNT);
        for (int i = PREKEY_START; i < PREKEY_START + PREKEY_COUNT; i++) {
            final PreKeyRecord preKeyRecord = this.preKeyStore.loadPreKey(i);
            this.preKeys.add(preKeyRecord);
        }

        final int signedPreKeyId = SignalPreferences.getSignedPreKeyId();
        this.signedPreKey = this.preKeyStore.loadSignedPreKey(signedPreKeyId);
    }

    private void generateKeys() throws InvalidKeyException {
        this.registrationId = KeyHelper.generateRegistrationId(false);
        this.identityKeyPair = KeyHelper.generateIdentityKeyPair();
        this.preKeys = KeyHelper.generatePreKeys(PREKEY_START, PREKEY_COUNT);
        this.signedPreKey = KeyHelper.generateSignedPreKey(identityKeyPair, SIGNED_PREKEY_ID);

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
        SignalPreferences.setLocalRegistrationId(this.registrationId);
        SignalPreferences.setSerializedIdentityKeyPair(this.identityKeyPair.serialize());
        SignalPreferences.setSignedPreKeyId(this.signedPreKey.getId());
    }

    private void createSession() throws InvalidKeyIdException {
        final SignalProtocolAddress remoteAddress = new SignalProtocolAddress("name", 1);
//        final SessionBuilder sessionBuilder = new SessionBuilder(
//                this.sessionStore,
//                this.preKeyStore,
//                this.preKeyStore,
//                this.identityStore,
//                remoteAddress
//        );
//
//        LogUtil.i("RegistrationId", String.valueOf(registrationId));
//        LogUtil.i("PreKeyId", String.valueOf(preKeys.get(0).getId()));
//        LogUtil.i("PreKey", Base64.toBase64String(preKeys.get(0).getKeyPair().getPublicKey().serialize()));
//        LogUtil.i("SignedPreKeyId", String.valueOf(signedPreKey.getId()));
//        LogUtil.i("SignedPreKey", Base64.toBase64String(signedPreKey.getKeyPair().getPublicKey().serialize()));
//        LogUtil.i("SignedPreKeySignature", Base64.toBase64String(signedPreKey.getSignature()));
//        LogUtil.i("IdentityKeyPair", Base64.toBase64String(identityKeyPair.serialize()));

        try {
//            final ECPublicKey remotePreKey = Curve.decodePoint(Base64.decode("BZ24gozdHGJqYaEb+qqL4cSSbzN6kozsD58svLR2+e5H"), 0);
//            final ECPublicKey remoteSignedPreKey = Curve.decodePoint(Base64.decode("BYFYTVfEDBGXqKiN7Bauco0nUtQ/jYD0pZwMF3/9Q6wt"), 0);
//            final byte[] remoteSignature = Base64.decode("0Wqs6h4COhDEkVtLlrjOkYwAuT2u/A+J+WRviXOdm/gnZ8BtXzkbFLFZyWkJk8RpcDd+TNvgsYcY+p9h5/gjhQ==");
//            final IdentityKeyPair remoteIdentityKeyPair = new IdentityKeyPair(Base64.decode("CiEFOxfqYwu9sH6SfsfKv2PPl9U9Gq83TANPQL5Q7Ijh2FISIIiVi+KHyF58T3X6OTkxfYe0GPno/xopyP1sP3Awh7dm"));
//            final PreKeyBundle bundle = new PreKeyBundle(9949,1,1234,remotePreKey,1,remoteSignedPreKey, remoteSignature, remoteIdentityKeyPair.getPublicKey());
//            sessionBuilder.process(bundle);
//
//            CiphertextMessage message = sessionCipher.encrypt("Hello world!".getBytes("UTF-8"));

            final SessionCipher sessionCipher = new SessionCipher(sessionStore, preKeyStore, preKeyStore, identityStore, remoteAddress);
            final PreKeySignalMessage message1 = new PreKeySignalMessage("wjSCRIhBXQ4EJsURZSNi6dRmsLYR3AW03Wzz+tRGsS2PPrJ6VM/GiEFjXTIyPPppeQWSCKaMtsGg/82/Kummh9SZOsyqG4Y8BEiQjMKIQVMTq8Q/PwmHhBl6X5qCM97zufrJ1X7hHzjcUmUz1sEPRACGAAiEGXsr5HD9Eh3KuYcXqfc+YUlQ0DjovQKIyiLCDAB".getBytes("UTF-8"));
            final byte[] decoded = sessionCipher.decrypt(message1);

            LogUtil.print(getClass(), new String(decoded, "UTF-8"));
        } catch (InvalidKeyException | UntrustedIdentityException | UnsupportedEncodingException | InvalidVersionException | InvalidMessageException | LegacyMessageException | DuplicateMessageException e) {
            e.printStackTrace();
        }

    }
}
