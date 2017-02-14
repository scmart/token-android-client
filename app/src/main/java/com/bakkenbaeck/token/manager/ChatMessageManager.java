package com.bakkenbaeck.token.manager;


import com.bakkenbaeck.token.BuildConfig;
import com.bakkenbaeck.token.R;
import com.bakkenbaeck.token.crypto.HDWallet;
import com.bakkenbaeck.token.crypto.signal.SignalPreferences;
import com.bakkenbaeck.token.crypto.signal.SignalService;
import com.bakkenbaeck.token.crypto.signal.store.ProtocolStore;
import com.bakkenbaeck.token.crypto.signal.store.SignalTrustStore;
import com.bakkenbaeck.token.manager.model.ChatMessageTask;
import com.bakkenbaeck.token.model.local.ChatMessage;
import com.bakkenbaeck.token.model.local.SendState;
import com.bakkenbaeck.token.model.local.User;
import com.bakkenbaeck.token.model.sofa.Payment;
import com.bakkenbaeck.token.model.sofa.PaymentRequest;
import com.bakkenbaeck.token.model.sofa.SofaAdapters;
import com.bakkenbaeck.token.model.sofa.SofaType;
import com.bakkenbaeck.token.presenter.store.ConversationStore;
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
import org.whispersystems.libsignal.util.guava.Optional;
import org.whispersystems.signalservice.api.SignalServiceMessagePipe;
import org.whispersystems.signalservice.api.SignalServiceMessageReceiver;
import org.whispersystems.signalservice.api.SignalServiceMessageSender;
import org.whispersystems.signalservice.api.crypto.SignalServiceCipher;
import org.whispersystems.signalservice.api.crypto.UntrustedIdentityException;
import org.whispersystems.signalservice.api.messages.SignalServiceContent;
import org.whispersystems.signalservice.api.messages.SignalServiceDataMessage;
import org.whispersystems.signalservice.api.messages.SignalServiceEnvelope;
import org.whispersystems.signalservice.api.push.SignalServiceAddress;
import org.whispersystems.signalservice.internal.push.SignalServiceUrl;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import rx.SingleSubscriber;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

public final class ChatMessageManager {
    private final PublishSubject<ChatMessageTask> chatMessageQueue = PublishSubject.create();

    private SignalService signalService;
    private HDWallet wallet;
    private SignalTrustStore trustStore;
    private ProtocolStore protocolStore;
    private SignalServiceMessagePipe messagePipe;
    private ConversationStore conversationStore;
    private ExecutorService dbThreadExecutor;
    private String userAgent;
    private SofaAdapters adapters;
    private boolean receiveMessages;
    private SignalServiceUrl[] signalServiceUrls;

    public final ChatMessageManager init(final HDWallet wallet) {
        this.wallet = wallet;
        this.userAgent = "Android " + BuildConfig.APPLICATION_ID + " - " + BuildConfig.VERSION_NAME +  ":" + BuildConfig.VERSION_CODE;
        this.adapters = new SofaAdapters();
        this.signalServiceUrls = new SignalServiceUrl[1];
        new Thread(() -> initEverything()).start();

        return this;
    }

    // Will send the message to a remote peer
    // and store the message in the local database
    public final void sendAndSaveMessage(final User receiver, final ChatMessage message) {
        final ChatMessageTask messageTask = new ChatMessageTask(receiver, message, ChatMessageTask.SEND_AND_SAVE);
        this.chatMessageQueue.onNext(messageTask);
    }

    // Will send the message to a remote peer
    // but not store the message in the local database
    public final void sendMessage(final User receiver, final ChatMessage message) {
        final ChatMessageTask messageTask = new ChatMessageTask(receiver, message, ChatMessageTask.SEND_ONLY);
        this.chatMessageQueue.onNext(messageTask);
    }

    // Will store the message in the local database
    // but not send the message to a remote peer
    public final void saveMessage(final User receiver, final ChatMessage message) {
        final ChatMessageTask messageTask = new ChatMessageTask(receiver, message, ChatMessageTask.SAVE_ONLY);
        this.chatMessageQueue.onNext(messageTask);
    }

    public final void resumeMessageReceiving() {
        if (haveRegisteredWithServer() && this.wallet != null) {
            receiveMessagesAsync();
        }
    }

    public final void disconnect() {
        this.receiveMessages = false;
        if (this.messagePipe != null) {
            this.messagePipe.shutdown();
            this.messagePipe = null;
        }
    }

    private void initEverything() {
        generateStores();
        initDatabase();
        registerIfNeeded();
        attachSubscribers();
    }

    private void initDatabase() {
        this.dbThreadExecutor = Executors.newSingleThreadExecutor();
        this.dbThreadExecutor.submit((Runnable) () -> ChatMessageManager.this.conversationStore = new ConversationStore());
    }

    private void generateStores() {
        this.protocolStore = new ProtocolStore().init();
        this.trustStore = new SignalTrustStore();

        final SignalServiceUrl signalServiceUrl = new SignalServiceUrl(
                BaseApplication.get().getResources().getString(R.string.chat_url),
                this.trustStore);
        this.signalServiceUrls[0] = signalServiceUrl;
        this.signalService = new SignalService(this.signalServiceUrls, this.wallet, this.protocolStore, this.userAgent);
    }

    private void registerIfNeeded() {
        if (!haveRegisteredWithServer()) {
            registerWithServer();
        } else {
            receiveMessagesAsync();
        }
    }

    private void registerWithServer() {
        this.signalService.registerKeys(
                this.protocolStore,
                new SingleSubscriber<Void>() {
                    @Override
                    public void onSuccess(final Void aVoid) {
                        SignalPreferences.setRegisteredWithServer();
                        receiveMessagesAsync();
                    }

                    @Override
                    public void onError(final Throwable throwable) {
                        LogUtil.e(getClass(), "Error during key registration: " + throwable);
                    }
                });
    }

    private boolean haveRegisteredWithServer() {
        return SignalPreferences.getRegisteredWithServer();
    }

    private void attachSubscribers() {
        this.chatMessageQueue
                .observeOn(Schedulers.io())
                .subscribeOn(Schedulers.io())
                .subscribe(new OnNextSubscriber<ChatMessageTask>() {
            @Override
            public void onNext(final ChatMessageTask messageTask) {
                dbThreadExecutor.submit(() -> {
                    if (messageTask.getAction() == ChatMessageTask.SEND_AND_SAVE) {
                        sendMessageToRemotePeer(messageTask.getReceiver(), messageTask.getChatMessage(), true);
                    } else if (messageTask.getAction() == ChatMessageTask.SAVE_ONLY) {
                        storeMessage(messageTask.getReceiver(), messageTask.getChatMessage());
                    } else {
                        sendMessageToRemotePeer(messageTask.getReceiver(), messageTask.getChatMessage(), false);
                    }
                });
            }
        });
    }

    private void sendMessageToRemotePeer(final User receiver, final ChatMessage message, final boolean saveMessageToDatabase) {
        final SignalServiceMessageSender messageSender = new SignalServiceMessageSender(
                this.signalServiceUrls,
                this.wallet.getOwnerAddress(),
                this.protocolStore.getPassword(),
                this.protocolStore,
                this.userAgent,
                Optional.absent(),
                Optional.absent()
        );

        if (saveMessageToDatabase) {
            this.conversationStore.saveNewMessage(receiver, message);
        }

        try {
            messageSender.sendMessage(
                    new SignalServiceAddress(receiver.getOwnerAddress()),
                    SignalServiceDataMessage.newBuilder()
                            .withBody(message.getAsSofaMessage())
                            .build());

            if (saveMessageToDatabase) {
                message.setSendState(SendState.STATE_SENT);
                this.conversationStore.updateMessage(receiver, message);
            }
        } catch (final UntrustedIdentityException | IOException ex) {
            LogUtil.error(getClass(), ex.toString());
            if (saveMessageToDatabase) {
                message.setSendState(SendState.STATE_FAILED);
                this.conversationStore.updateMessage(receiver, message);
            }
        }
    }

    private void storeMessage(final User receiver, final ChatMessage message) {
        message.setSendState(SendState.STATE_LOCAL_ONLY);
        this.conversationStore.saveNewMessage(receiver, message);
    }

    private void receiveMessagesAsync() {
        this.receiveMessages = true;
        new Thread(() -> {
            while (receiveMessages) {
                receiveMessages();
            }
        }).start();
    }

    private void receiveMessages() {
        final SignalServiceUrl[] urls = {
                new SignalServiceUrl(
                        BaseApplication.get().getResources().getString(R.string.chat_url),
                        this.trustStore
                )
        };

        final SignalServiceMessageReceiver messageReceiver = new SignalServiceMessageReceiver(
                urls,
                this.wallet.getOwnerAddress(),
                this.protocolStore.getPassword(),
                this.protocolStore.getSignalingKey(),
                this.userAgent);
        final SignalServiceAddress localAddress = new SignalServiceAddress(this.wallet.getOwnerAddress());
        final SignalServiceCipher cipher = new SignalServiceCipher(localAddress, this.protocolStore);


        if (this.messagePipe == null) {
            this.messagePipe = messageReceiver.createMessagePipe();
        }

        try {
            final SignalServiceEnvelope envelope = messagePipe.read(10, TimeUnit.SECONDS);
            final SignalServiceContent message = cipher.decrypt(envelope);
            final Optional<SignalServiceDataMessage> dataMessage = message.getDataMessage();
            if (dataMessage.isPresent()) {
                final String messageSource = envelope.getSource();
                final Optional<String> messageBody = dataMessage.get().getBody();
                if (messageBody.isPresent()) {
                    saveIncomingMessageToDatabase(messageSource, messageBody.get());
                }
            }
        } catch (final TimeoutException ex) {
            // Nop. This is to be expected
        } catch (final IllegalStateException | InvalidKeyException | InvalidKeyIdException | DuplicateMessageException | InvalidVersionException | LegacyMessageException | InvalidMessageException | NoSessionException | org.whispersystems.libsignal.UntrustedIdentityException | IOException e) {
            LogUtil.e(getClass(), "receiveMessage: " + e.toString());
        }
    }

    private void saveIncomingMessageToDatabase(final String messageSource, final String messageBody) {
        BaseApplication
        .get()
        .getTokenManager()
        .getUserManager()
        .getUserFromAddress(messageSource)
        .subscribe(new OnNextSubscriber<User>() {
            @Override
            public void onNext(final User user) {
                if (user == null) {
                    return;
                }
                unsubscribe();

                final User threadSafeUser = new User(user);

                final ChatMessage remoteMessage = new ChatMessage().makeNew(false, messageBody);
                if (remoteMessage.getType() == SofaType.PAYMENT) {
                    sendIncomingPaymentToTransactionManager(threadSafeUser, remoteMessage);
                    return;
                } else if(remoteMessage.getType() == SofaType.PAYMENT_REQUEST) {
                    embedLocalAmountIntoPaymentRequest(remoteMessage);
                }

                dbThreadExecutor.execute(() -> ChatMessageManager.this.conversationStore.saveNewMessage(threadSafeUser, remoteMessage));
            }
        });
    }

    private void sendIncomingPaymentToTransactionManager(final User sender, final ChatMessage remoteMessage) {
        try {
            final Payment payment = adapters.paymentFrom(remoteMessage.getPayload());
            payment.generateLocalPrice();

            BaseApplication
                    .get()
                    .getTokenManager()
                    .getTransactionManager()
                    .processIncomingPayment(sender, payment);
        } catch (IOException e) {
            // No-op
        }
    }

    private void embedLocalAmountIntoPaymentRequest(final ChatMessage remoteMessage) {
        try {
            final PaymentRequest request = adapters.txRequestFrom(remoteMessage.getPayload());
            request.generateLocalPrice();
            remoteMessage.setPayload(adapters.toJson(request));
        } catch (IOException e) {
            // No-op
        }
    }
}
