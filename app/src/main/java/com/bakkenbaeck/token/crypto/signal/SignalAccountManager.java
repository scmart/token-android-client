package com.bakkenbaeck.token.crypto.signal;


import com.bakkenbaeck.token.BuildConfig;
import com.bakkenbaeck.token.R;
import com.bakkenbaeck.token.crypto.HDWallet;
import com.bakkenbaeck.token.crypto.signal.model.OutgoingSignedPreKeyState;
import com.bakkenbaeck.token.crypto.signal.model.PreKeyStateWithTimestamp;
import com.bakkenbaeck.token.view.BaseApplication;

import org.whispersystems.libsignal.IdentityKey;
import org.whispersystems.libsignal.state.PreKeyRecord;
import org.whispersystems.libsignal.state.SignedPreKeyRecord;
import org.whispersystems.signalservice.api.SignalServiceAccountManager;
import org.whispersystems.signalservice.api.push.SignedPreKeyEntity;
import org.whispersystems.signalservice.api.push.TrustStore;
import org.whispersystems.signalservice.internal.push.PreKeyEntity;
import org.whispersystems.signalservice.internal.util.JsonUtil;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/* package */ class SignalAccountManager extends SignalServiceAccountManager {

    private static final String PREKEY_PATH = "/keys/";
    private final HDWallet wallet;

    /* package */ SignalAccountManager(final TrustStore trustStore, final HDWallet wallet) {
        this(BaseApplication.get().getResources().getString(R.string.signal_url),
                trustStore,
                wallet,
                "unused",
                "unused",
                "Android " + BuildConfig.APPLICATION_ID + " - " + BuildConfig.VERSION_NAME +  ":" + BuildConfig.VERSION_CODE);
    }

    private SignalAccountManager(final String url,
                                 final TrustStore trustStore,
                                 final HDWallet wallet,
                                 final String user,
                                 final String password,
                                 final String userAgent) {
        super(url, trustStore, user, password, userAgent);
        this.wallet = wallet;
    }

    /* package */ void registerKeys(
             final IdentityKey identityKey,
             final PreKeyRecord lastResortKey,
             final int registrationId,
             final String signalingKey,
             final SignedPreKeyRecord signedPreKey,
             final List<PreKeyRecord> preKeys) throws IOException {
        
        final List<PreKeyEntity> entities = new LinkedList<>();
        for (PreKeyRecord preKey : preKeys) {
            final PreKeyEntity entity = new PreKeyEntity(
                    preKey.getId(),
                    preKey.getKeyPair().getPublicKey());
            entities.add(entity);
        }

        final PreKeyEntity lastResortEntity = new PreKeyEntity(
                lastResortKey.getId(),
                lastResortKey.getKeyPair().getPublicKey());

        final SignedPreKeyEntity signedPreKeyEntity = new SignedPreKeyEntity(
                signedPreKey.getId(),
                signedPreKey.getKeyPair().getPublicKey(),
                signedPreKey.getSignature());

        final long timestamp = System.currentTimeMillis();

        final PreKeyStateWithTimestamp payload = new PreKeyStateWithTimestamp(
                entities,
                lastResortEntity,
                registrationId,
                signalingKey,
                signedPreKeyEntity,
                identityKey,
                timestamp);
        final String payloadForSigning = JsonUtil.toJson(payload);
        final String signature = this.wallet.signString(payloadForSigning);
        final OutgoingSignedPreKeyState outgoingEvent = new OutgoingSignedPreKeyState(payload, signature);

        super.setPreKeysWithSignature(PREKEY_PATH, JsonUtil.toJson(outgoingEvent));
    }
}
