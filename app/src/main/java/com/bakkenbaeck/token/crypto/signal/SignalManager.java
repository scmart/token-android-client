package com.bakkenbaeck.token.crypto.signal;


import com.bakkenbaeck.token.R;
import com.bakkenbaeck.token.crypto.HDWallet;
import com.bakkenbaeck.token.crypto.signal.store.ProtocolStore;
import com.bakkenbaeck.token.crypto.signal.store.SignalTrustStore;
import com.bakkenbaeck.token.view.BaseApplication;

import org.whispersystems.signalservice.api.SignalServiceMessageSender;
import org.whispersystems.signalservice.api.crypto.UntrustedIdentityException;
import org.whispersystems.signalservice.api.messages.SignalServiceDataMessage;
import org.whispersystems.signalservice.api.push.SignalServiceAddress;

import java.io.IOException;

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
        sendMessage();
    }

    private void generateStores() {
        this.protocolStore = new ProtocolStore().init();
        this.trustStore = new SignalTrustStore();
        this.accountManager = new SignalAccountManager(this.trustStore, this.wallet);
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
                "unused",
                this.protocolStore.getPassword(),
                this.protocolStore,
                "ToDo",
                null
        );
        try {
            messageSender.sendMessage(
                    new SignalServiceAddress("+14159998888"),
                        SignalServiceDataMessage.newBuilder()
                            .withBody("Hello, world!")
                            .build());
        } catch (final UntrustedIdentityException | IOException ex) {
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
