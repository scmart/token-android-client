package com.bakkenbaeck.token.manager;


import com.bakkenbaeck.token.BuildConfig;
import com.bakkenbaeck.token.R;
import com.bakkenbaeck.token.crypto.HDWallet;
import com.bakkenbaeck.token.crypto.signal.SignalPreferences;
import com.bakkenbaeck.token.crypto.signal.SignalService;
import com.bakkenbaeck.token.crypto.signal.store.ProtocolStore;
import com.bakkenbaeck.token.crypto.signal.store.SignalTrustStore;
import com.bakkenbaeck.token.model.ChatMessage;
import com.bakkenbaeck.token.presenter.store.ChatMessageStore;
import com.bakkenbaeck.token.util.LogUtil;
import com.bakkenbaeck.token.util.OnNextSubscriber;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import rx.Observable;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

import static com.bakkenbaeck.token.model.ChatMessage.STATE_FAILED;
import static com.bakkenbaeck.token.model.ChatMessage.STATE_SENT;

public final class SignalManager {
    private final PublishSubject<ChatMessage> sendMessageSubject = PublishSubject.create();

    private SignalService signalService;
    private HDWallet wallet;
    private SignalTrustStore trustStore;
    private ProtocolStore protocolStore;
    private SignalServiceMessagePipe messagePipe;
    private ChatMessageStore chatMessageStore;
    private ExecutorService dbThreadExecutor;
    private String userAgent;
    private boolean receiveMessages;

    public final SignalManager init(final HDWallet wallet) {
        this.wallet = wallet;
        this.userAgent = "Android " + BuildConfig.APPLICATION_ID + " - " + BuildConfig.VERSION_NAME +  ":" + BuildConfig.VERSION_CODE;
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
        initDatabase();
        registerIfNeeded();
        attachSubscribers();
    }

    private void initDatabase() {
        this.dbThreadExecutor = Executors.newSingleThreadExecutor();
        this.dbThreadExecutor.submit(new Runnable() {
            @Override
            public void run() {
                SignalManager.this.chatMessageStore = new ChatMessageStore();
            }
        });
    }

    private void generateStores() {
        this.protocolStore = new ProtocolStore().init();
        this.trustStore = new SignalTrustStore();
        this.signalService = new SignalService(this.trustStore, this.wallet, this.protocolStore, this.userAgent);
    }

    private void registerIfNeeded() {
        if (!haveRegisteredWithServer()) {
            registerWithServer();
        }
    }

    private void registerWithServer() {
        this.signalService.registerKeys(this.protocolStore);
        SignalPreferences.setRegisteredWithServer();
    }

    private boolean haveRegisteredWithServer() {
        return SignalPreferences.getRegisteredWithServer();
    }

    private void attachSubscribers() {
        this.sendMessageSubject
                .observeOn(Schedulers.io())
                .subscribeOn(Schedulers.io())
                .subscribe(new OnNextSubscriber<ChatMessage>() {
            @Override
            public void onNext(final ChatMessage message) {
                dbThreadExecutor.submit(new Runnable() {
                    @Override
                    public void run() {
                        sendMessageToBackend(message);
                    }
                });
            }
        });

        receiveMessagesAsync();
    }



    public final void sendMessage(final ChatMessage message) {
        this.sendMessageSubject.onNext(message);
    }

    private void sendMessageToBackend(final ChatMessage message) {
        final SignalServiceMessageSender messageSender = new SignalServiceMessageSender(
                BaseApplication.get().getResources().getString(R.string.chat_url),
                this.trustStore,
                this.wallet.getAddress(),
                this.protocolStore.getPassword(),
                this.protocolStore,
                this.userAgent,
                null
        );

        chatMessageStore.save(message);

        try {
            messageSender.sendMessage(
                    new SignalServiceAddress(message.getConversationId()),
                    SignalServiceDataMessage.newBuilder()
                            .withBody(message.getText())
                            .build());
            this.chatMessageStore.setSendState(message, STATE_SENT);
        } catch (final UntrustedIdentityException | IOException ex) {
            LogUtil.error(getClass(), ex.toString());
            this.chatMessageStore.setSendState(message, STATE_FAILED);
        }
    }

    public final void connect() {
        receiveMessagesAsync();
    }

    public final void disconnect() {
        this.receiveMessages = false;
        if (this.messagePipe != null) {
            this.messagePipe.shutdown();
            this.messagePipe = null;
        }
    }

    private void receiveMessagesAsync() {
        this.receiveMessages = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (receiveMessages) {
                    receiveMessages();
                }
            }
        }).start();
    }

    private void receiveMessages() {
        final SignalServiceMessageReceiver messageReceiver = new SignalServiceMessageReceiver(
                BaseApplication.get().getResources().getString(R.string.chat_url),
                this.trustStore,
                this.wallet.getAddress(),
                this.protocolStore.getPassword(),
                this.protocolStore.getSignalingKey(),
                this.userAgent);
        final SignalServiceAddress localAddress = new SignalServiceAddress(this.wallet.getAddress());
        final SignalServiceCipher cipher = new SignalServiceCipher(localAddress, this.protocolStore);


        if (this.messagePipe == null) {
            this.messagePipe = messageReceiver.createMessagePipe();
        }

        try {
            final SignalServiceEnvelope envelope = messagePipe.read(10, TimeUnit.SECONDS);
            final SignalServiceContent message = cipher.decrypt(envelope);
            final SignalServiceDataMessage dataMessage = message.getDataMessage().get();
            if (dataMessage != null) {
                final String messageSource = envelope.getSource();
                final String messageBody = dataMessage.getBody().get();
                saveMessageToDatabase(messageSource, messageBody);
            }
        } catch (final TimeoutException ex) {
            // Nop. This is to be expected
        } catch (final IllegalStateException | InvalidKeyException | InvalidKeyIdException | DuplicateMessageException | InvalidVersionException | LegacyMessageException | InvalidMessageException | NoSessionException | org.whispersystems.libsignal.UntrustedIdentityException | IOException e) {
            LogUtil.e(getClass(), "receiveMessage: " + e.toString());
        }
    }

    private void saveMessageToDatabase(final String messageSource, final String messageBody) {
        this.dbThreadExecutor.submit(new Runnable() {
            @Override
            public void run() {
                final ChatMessage remoteMessage = new ChatMessage().makeRemoteMessage(messageSource, messageBody);
                SignalManager.this.chatMessageStore.save(remoteMessage);
            }
        });
    }
}
