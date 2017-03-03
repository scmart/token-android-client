package com.tokenbrowser.manager;


import android.content.Context;
import android.content.SharedPreferences;
import android.util.Pair;

import com.tokenbrowser.crypto.HDWallet;
import com.tokenbrowser.crypto.signal.SignalPreferences;
import com.tokenbrowser.crypto.signal.SignalService;
import com.tokenbrowser.crypto.signal.model.DecryptedSignalMessage;
import com.tokenbrowser.crypto.signal.store.ProtocolStore;
import com.tokenbrowser.crypto.signal.store.SignalTrustStore;
import com.tokenbrowser.manager.model.SofaMessageTask;
import com.tokenbrowser.manager.network.IdService;
import com.tokenbrowser.manager.store.ConversationStore;
import com.tokenbrowser.model.local.Conversation;
import com.tokenbrowser.model.local.SendState;
import com.tokenbrowser.model.local.SofaMessage;
import com.tokenbrowser.model.local.User;
import com.tokenbrowser.model.network.UserSearchResults;
import com.tokenbrowser.model.sofa.Message;
import com.tokenbrowser.model.sofa.PaymentRequest;
import com.tokenbrowser.model.sofa.SofaAdapters;
import com.tokenbrowser.model.sofa.SofaType;
import com.tokenbrowser.service.RegistrationIntentService;
import com.tokenbrowser.token.BuildConfig;
import com.tokenbrowser.token.R;
import com.tokenbrowser.util.FileNames;
import com.tokenbrowser.util.LogUtil;
import com.tokenbrowser.util.OnNextSubscriber;
import com.tokenbrowser.util.SharedPrefsUtil;
import com.tokenbrowser.view.BaseApplication;
import com.tokenbrowser.view.notification.ChatNotificationManager;

import org.whispersystems.libsignal.DuplicateMessageException;
import org.whispersystems.libsignal.InvalidKeyException;
import org.whispersystems.libsignal.InvalidKeyIdException;
import org.whispersystems.libsignal.InvalidMessageException;
import org.whispersystems.libsignal.InvalidVersionException;
import org.whispersystems.libsignal.LegacyMessageException;
import org.whispersystems.libsignal.NoSessionException;
import org.whispersystems.libsignal.SignalProtocolAddress;
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
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import rx.Single;
import rx.SingleSubscriber;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

public final class SofaMessageManager {

    private static final String ONBOARDING_BOT_NAME = "TokenBot";
    private final PublishSubject<SofaMessageTask> chatMessageQueue = PublishSubject.create();

    private SharedPreferences sharedPreferences;
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
    private String gcmToken;

    public final SofaMessageManager init(final HDWallet wallet) {
        this.wallet = wallet;
        this.userAgent = "Android " + BuildConfig.APPLICATION_ID + " - " + BuildConfig.VERSION_NAME +  ":" + BuildConfig.VERSION_CODE;
        this.adapters = new SofaAdapters();
        this.signalServiceUrls = new SignalServiceUrl[1];
        this.sharedPreferences = BaseApplication.get().getSharedPreferences(FileNames.GCM_PREFS, Context.MODE_PRIVATE);
        new Thread(() -> initEverything()).start();

        return this;
    }

    public void setGcmToken(final String token) {
        this.gcmToken = token;
        tryRegisterGcm();
    }

    private void tryRegisterGcm() {
        if (this.gcmToken == null) {
            return;
        }

        if (this.sharedPreferences.getBoolean(RegistrationIntentService.CHAT_SERVICE_SENT_TOKEN_TO_SERVER, false)) {
            // Already registered
            return;
        }
        try {
            final Optional<String> optional = Optional.of(this.gcmToken);
            this.signalService.setGcmId(optional);
            this.sharedPreferences.edit().putBoolean
                    (RegistrationIntentService.CHAT_SERVICE_SENT_TOKEN_TO_SERVER, true).apply();
            this.gcmToken = null;
        } catch (IOException e) {
            this.sharedPreferences.edit().putBoolean
                    (RegistrationIntentService.CHAT_SERVICE_SENT_TOKEN_TO_SERVER, false).apply();
            LogUtil.d(getClass(), "Error during registering of GCM " + e.getMessage());
        }
    }

    // Will send the message to a remote peer
    // and store the message in the local database
    public final void sendAndSaveMessage(final User receiver, final SofaMessage message) {
        final SofaMessageTask messageTask = new SofaMessageTask(receiver, message, SofaMessageTask.SEND_AND_SAVE);
        this.chatMessageQueue.onNext(messageTask);
    }

    // Will send the message to a remote peer
    // but not store the message in the local database
    public final void sendMessage(final User receiver, final SofaMessage message) {
        final SofaMessageTask messageTask = new SofaMessageTask(receiver, message, SofaMessageTask.SEND_ONLY);
        this.chatMessageQueue.onNext(messageTask);
    }

    // Will store the message in the local database
    // but not send the message to a remote peer
    public final void saveMessage(final User receiver, final SofaMessage message) {
        final SofaMessageTask messageTask = new SofaMessageTask(receiver, message, SofaMessageTask.SAVE_ONLY);
        this.chatMessageQueue.onNext(messageTask);
    }

    // Will store a tranasaction in the local database
    // but not send the message to a remote peer. It will also save the state as "SENDING".
    /* package */ final void saveTransaction(final User receiver, final SofaMessage message) {
        final SofaMessageTask messageTask = new SofaMessageTask(receiver, message, SofaMessageTask.SAVE_TRANSACTION);
        this.chatMessageQueue.onNext(messageTask);
    }

    // Updates a pre-existing message.
    /* package */ final void updateMessage(final User receiver, final SofaMessage message) {
        final SofaMessageTask messageTask = new SofaMessageTask(receiver, message, SofaMessageTask.UPDATE_MESSAGE);
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

    public final Single<List<Conversation>> loadAllConversations() {
        return Single
                .fromCallable(() -> conversationStore.loadAll())
                .subscribeOn(Schedulers.from(this.dbThreadExecutor));
    }

    public final Single<Conversation> loadConversation(final String conversationId) {
        return Single
                .fromCallable(() -> conversationStore.loadByAddress(conversationId))
                .subscribeOn(Schedulers.from(this.dbThreadExecutor));
    }

    public final PublishSubject<Conversation> registerForAllConversationChanges() {
        return this.conversationStore.getConversationChangedObservable();
    }

    // Returns a pair of RxSubjects, the first being the observable for new messages
    // the second being the observable for updated messages.
    public final Pair<PublishSubject<SofaMessage>, PublishSubject<SofaMessage>> registerForConversationChanges(final String conversationId) {
        return this.conversationStore.registerForChanges(conversationId);
    }

    public final void stopListeningForConversationChanges() {
        this.conversationStore.stopListeningForChanges();
    }

    public final Single<Boolean> isReady() {
        return Single
                .fromCallable(() -> {
                    while (this.conversationStore == null) {
                        Thread.sleep(50);
                    }
                    return true;
                })
                .subscribeOn(Schedulers.io());
    }

    public final Single<Boolean> areUnreadMessages() {
        return Single
                .fromCallable(() -> conversationStore.areUnreadMessages())
                .subscribeOn(Schedulers.from(this.dbThreadExecutor));
    }

    private void initEverything() {
        generateStores();
        initDatabase();
        registerIfNeeded();
        attachSubscribers();
    }

    private void initDatabase() {
        this.dbThreadExecutor = Executors.newSingleThreadExecutor();
        this.dbThreadExecutor.submit((Runnable) () -> SofaMessageManager.this.conversationStore = new ConversationStore());
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
            tryRegisterGcm();
            tryTriggerOnboarding();
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
                        tryRegisterGcm();
                        tryTriggerOnboarding();
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
                .subscribe(new OnNextSubscriber<SofaMessageTask>() {
            @Override
            public void onNext(final SofaMessageTask messageTask) {
                dbThreadExecutor.submit(() -> {
                    switch (messageTask.getAction()) {
                        case SofaMessageTask.SEND_AND_SAVE:
                            sendMessageToRemotePeer(messageTask.getReceiver(), messageTask.getSofaMessage(), true);
                            break;
                        case SofaMessageTask.SAVE_ONLY:
                            storeMessage(messageTask.getReceiver(), messageTask.getSofaMessage(), SendState.STATE_LOCAL_ONLY);
                            break;
                        case SofaMessageTask.SAVE_TRANSACTION:
                            storeMessage(messageTask.getReceiver(), messageTask.getSofaMessage(), SendState.STATE_SENDING);
                            break;
                        case SofaMessageTask.SEND_ONLY:
                            sendMessageToRemotePeer(messageTask.getReceiver(), messageTask.getSofaMessage(), false);
                            break;
                        case SofaMessageTask.UPDATE_MESSAGE:
                            updateExistingMessage(messageTask.getReceiver(), messageTask.getSofaMessage());
                            break;
                    }
                });
            }
        });
    }

    private void sendMessageToRemotePeer(final User receiver, final SofaMessage message, final boolean saveMessageToDatabase) {
        if (saveMessageToDatabase) {
            this.conversationStore.saveNewMessage(receiver, message);
        }

        try {
            sendToSignal(receiver, message);

            if (saveMessageToDatabase) {
                message.setSendState(SendState.STATE_SENT);
                updateExistingMessage(receiver, message);
            }
        } catch (final UntrustedIdentityException ue) {
            LogUtil.error(getClass(), "Keys have changed. " + ue);
            protocolStore.saveIdentity(
                    new SignalProtocolAddress(receiver.getOwnerAddress(), SignalServiceAddress.DEFAULT_DEVICE_ID),
                    ue.getIdentityKey());
        } catch (final IOException ex) {
            LogUtil.error(getClass(), ex.toString());
            if (saveMessageToDatabase) {
                message.setSendState(SendState.STATE_FAILED);
                updateExistingMessage(receiver, message);
            }
        }
    }

    private void sendToSignal(final User receiver, final SofaMessage message) throws UntrustedIdentityException, IOException {
        final SignalServiceMessageSender messageSender = new SignalServiceMessageSender(
                this.signalServiceUrls,
                this.wallet.getOwnerAddress(),
                this.protocolStore.getPassword(),
                this.protocolStore,
                this.userAgent,
                Optional.absent(),
                Optional.absent()
        );

        messageSender.sendMessage(
                new SignalServiceAddress(receiver.getOwnerAddress()),
                SignalServiceDataMessage.newBuilder()
                        .withBody(message.getAsSofaMessage())
                        .build());
    }

    private void storeMessage(final User receiver, final SofaMessage message, final @SendState.State int sendState) {
        message.setSendState(sendState);
        this.conversationStore.saveNewMessage(receiver, message);
    }

    private void updateExistingMessage(final User receiver, final SofaMessage message) {
        this.conversationStore.updateMessage(receiver, message);
    }

    private void receiveMessagesAsync() {
        if (this.receiveMessages) {
            // Already running.
            return;
        }

        this.receiveMessages = true;
        new Thread(() -> {
            while (receiveMessages) {
                try {
                    final DecryptedSignalMessage signalMessage = fetchLatestMessage();
                    ChatNotificationManager.showNotification(signalMessage);
                } catch (TimeoutException e) {
                    // Nop -- this is expected to happen
                }
            }
        }).start();
    }

    public DecryptedSignalMessage fetchLatestMessage() throws TimeoutException {
        if (!waitForWallet()) return null;

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

        if (this.messagePipe == null) {
            this.messagePipe = messageReceiver.createMessagePipe();
        }

        try {
            final SignalServiceEnvelope envelope = messagePipe.read(10, TimeUnit.SECONDS);
            return decryptIncomingSignalServiceEnvelope(envelope);
        } catch (final TimeoutException ex) {
            throw new TimeoutException(ex.getMessage());
        } catch (final IllegalStateException | InvalidKeyException | InvalidKeyIdException | DuplicateMessageException | InvalidVersionException | LegacyMessageException | InvalidMessageException | NoSessionException | org.whispersystems.libsignal.UntrustedIdentityException | IOException e) {
            LogUtil.e(getClass(), "receiveMessage: " + e.toString());
        }
        return null;
    }

    private boolean waitForWallet() {
        try {
            while (this.wallet == null) {
                Thread.sleep(200);
            }
        } catch (final InterruptedException e) {
            return false;
        }
        return true;
    }

    private DecryptedSignalMessage decryptIncomingSignalServiceEnvelope(final SignalServiceEnvelope envelope) throws InvalidVersionException, InvalidMessageException, InvalidKeyException, DuplicateMessageException, InvalidKeyIdException, org.whispersystems.libsignal.UntrustedIdentityException, LegacyMessageException, NoSessionException {
        // ToDo -- When do we need to create new keys?
 /*       if (envelope.getType() == SignalServiceProtos.Envelope.Type.PREKEY_BUNDLE_VALUE) {
            // New keys need to be registered with the server.
            registerWithServer();
            return;
        }*/
        return handleIncomingSofaMessage(envelope);
    }

    private DecryptedSignalMessage handleIncomingSofaMessage(final SignalServiceEnvelope envelope) throws InvalidVersionException, InvalidMessageException, InvalidKeyException, DuplicateMessageException, InvalidKeyIdException, org.whispersystems.libsignal.UntrustedIdentityException, LegacyMessageException, NoSessionException {
        final SignalServiceAddress localAddress = new SignalServiceAddress(this.wallet.getOwnerAddress());
        final SignalServiceCipher cipher = new SignalServiceCipher(localAddress, this.protocolStore);
        final SignalServiceContent message = cipher.decrypt(envelope);
        final Optional<SignalServiceDataMessage> dataMessage = message.getDataMessage();
        if (dataMessage.isPresent()) {
            final String messageSource = envelope.getSource();
            final Optional<String> messageBody = dataMessage.get().getBody();
            final DecryptedSignalMessage decryptedMessage = new DecryptedSignalMessage(messageSource, messageBody.get());
            saveIncomingMessageToDatabase(decryptedMessage);
            return decryptedMessage;
        }
        return null;
    }

    private void saveIncomingMessageToDatabase(final DecryptedSignalMessage signalMessage) {
        if (signalMessage == null || signalMessage.getBody() == null || signalMessage.getSource() == null) {
            LogUtil.w(getClass(), "Attempt to save invalid DecryptedSignalMessage to database.");
            return;
        }

        BaseApplication
        .get()
        .getTokenManager()
        .getUserManager()
        .getUserFromAddress(signalMessage.getSource())
        .subscribe((user) -> this.saveIncomingMessageFromUserToDatabase(user, signalMessage));
    }

    private void saveIncomingMessageFromUserToDatabase(final User user, final DecryptedSignalMessage signalMessage) {
        final SofaMessage remoteMessage = new SofaMessage().makeNew(false, signalMessage.getBody());
        if (remoteMessage.getType() == SofaType.PAYMENT) {
            // Don't render incoming SOFA::Payments,
            // but ensure we have the sender cached.
            fetchAndCacheIncomingPaymentSender(user);
            return;
        } else if(remoteMessage.getType() == SofaType.PAYMENT_REQUEST) {
            generatePayloadWithLocalAmountEmbedded(remoteMessage)
                    .subscribe((updatedPayload) -> {
                        remoteMessage.setPayload(updatedPayload);
                        dbThreadExecutor.execute(() -> SofaMessageManager.this.conversationStore.saveNewMessage(user, remoteMessage));
                    });
            return;
        }

        dbThreadExecutor.execute(() -> SofaMessageManager.this.conversationStore.saveNewMessage(user, remoteMessage));
    }

    private void fetchAndCacheIncomingPaymentSender(final User sender) {
        BaseApplication
        .get()
        .getTokenManager()
        .getUserManager()
        .getUserFromAddress(sender.getOwnerAddress());
    }

    private Single<String> generatePayloadWithLocalAmountEmbedded(final SofaMessage remoteMessage) {
        try {
            final PaymentRequest request = adapters.txRequestFrom(remoteMessage.getPayload());
            return request
                    .generateLocalPrice()
                    .map((updatedPaymentRequest) -> adapters.toJson(updatedPaymentRequest));
        } catch (final IOException ex) {
            LogUtil.e(getClass(), "Unable to embed local price");
        }

        return Single.just(remoteMessage.getPayloadWithHeaders());
    }


    private void tryTriggerOnboarding() {
        if (SharedPrefsUtil.hasOnboarded()) {
            return;
        }

        IdService.getApi()
                .searchByUsername(ONBOARDING_BOT_NAME)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(this::handleOnboardingBotFound);
    }

    private void handleOnboardingBotFound(final UserSearchResults results) {
        final List<User> users = results.getResults();
        for (final User user : users) {
            if (user.getUsernameForEditing().equals(ONBOARDING_BOT_NAME)) {
                BaseApplication
                        .get()
                        .getTokenManager()
                        .getSofaMessageManager()
                        .sendMessage(user, generateOnboardingMessage());
                SharedPrefsUtil.setHasOnboarded();
                break;
            }
        }
    }

    private SofaMessage generateOnboardingMessage() {
        final Message sofaMessage = new Message().setBody("");
        final String messageBody = new SofaAdapters().toJson(sofaMessage);
        return new SofaMessage().makeNew(true, messageBody);
    }
}
