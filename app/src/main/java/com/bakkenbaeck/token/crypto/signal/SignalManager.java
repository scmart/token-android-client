package com.bakkenbaeck.token.crypto.signal;


import com.bakkenbaeck.token.R;
import com.bakkenbaeck.token.crypto.HDWallet;
import com.bakkenbaeck.token.crypto.signal.store.ProtocolStore;
import com.bakkenbaeck.token.crypto.signal.store.SignalTrustStore;
import com.bakkenbaeck.token.util.LogUtil;
import com.bakkenbaeck.token.view.BaseApplication;

import org.whispersystems.libsignal.DuplicateMessageException;
import org.whispersystems.libsignal.InvalidKeyException;
import org.whispersystems.libsignal.InvalidKeyIdException;
import org.whispersystems.libsignal.InvalidMessageException;
import org.whispersystems.libsignal.InvalidVersionException;
import org.whispersystems.libsignal.LegacyMessageException;
import org.whispersystems.libsignal.NoSessionException;
import org.whispersystems.signalservice.api.SignalServiceMessagePipe;
import org.whispersystems.signalservice.api.SignalServiceMessageReceiver;
import org.whispersystems.signalservice.api.SignalServiceMessageSender;
import org.whispersystems.signalservice.api.crypto.SignalServiceCipher;
import org.whispersystems.signalservice.api.crypto.UntrustedIdentityException;
import org.whispersystems.signalservice.api.messages.SignalServiceContent;
import org.whispersystems.signalservice.api.messages.SignalServiceDataMessage;
import org.whispersystems.signalservice.api.messages.SignalServiceEnvelope;
import org.whispersystems.signalservice.api.push.SignalServiceAddress;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class SignalManager {

    private SignalAccountManager accountManager;
    private HDWallet wallet;
    private SignalTrustStore trustStore;
    private ProtocolStore protocolStore;

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
        registerIfNeeded();
    }

    private void receiveMessage() {
        SignalServiceMessageReceiver messageReciever = new SignalServiceMessageReceiver(
                BaseApplication.get().getResources().getString(R.string.signal_url),
                this.trustStore,
                this.wallet.getAddress(),
                this.protocolStore.getPassword(),
                this.protocolStore.getSignalingKey(),
                "userAgent");
        SignalServiceMessagePipe messagePipe = null;

        try {
            messagePipe = messageReciever.createMessagePipe();

            while (true) {
                SignalServiceEnvelope envelope = messagePipe.read(10, TimeUnit.SECONDS);
                SignalServiceCipher cipher = new SignalServiceCipher(new SignalServiceAddress(this.wallet.getAddress()),this.protocolStore);
                SignalServiceContent message = cipher.decrypt(envelope);

                System.out.println("Received message: " + message.getDataMessage().get().getBody().get());
            }

        } catch (InvalidKeyException | InvalidKeyIdException | DuplicateMessageException | InvalidVersionException | LegacyMessageException | InvalidMessageException | NoSessionException | org.whispersystems.libsignal.UntrustedIdentityException | IOException | TimeoutException e) {
            e.printStackTrace();
        } finally {
            if (messagePipe != null)
                messagePipe.shutdown();
        }
    }

    private void generateStores() {
        this.protocolStore = new ProtocolStore().init();
        this.trustStore = new SignalTrustStore();
        this.accountManager = new SignalAccountManager(this.trustStore, this.wallet, this.protocolStore);
    }

    private void registerIfNeeded() {
        if (!haveRegisteredWithServer()) {
            registerWithServer();
        }
    }

    private void sendMessage() {
        final SignalServiceMessageSender messageSender = new SignalServiceMessageSender(
                BaseApplication.get().getResources().getString(R.string.signal_url),
                this.trustStore,
                this.wallet.getAddress(),
                this.protocolStore.getPassword(),
                this.protocolStore,
                "Android v0.1",
                null
        );
        try {
            messageSender.sendMessage(
                    new SignalServiceAddress("43737354d47935d79f6ee51a5a6ab0ce5ef277db"),
                        SignalServiceDataMessage.newBuilder()
                            .withBody("Hello, world!")
                            .build());
        } catch (final UntrustedIdentityException | IOException ex) {
            LogUtil.error(getClass(), ex.toString());
            throw new RuntimeException(ex);
        }
    }

    private void registerWithServer() {
        this.accountManager.registerKeys(this.protocolStore);
        SignalPreferences.setRegisteredWithServer();
    }

    private boolean haveRegisteredWithServer() {
        return SignalPreferences.getRegisteredWithServer();
    }
}
