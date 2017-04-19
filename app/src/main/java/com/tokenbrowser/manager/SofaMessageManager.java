/*
 * 	Copyright (c) 2017. Token Browser, Inc
 *
 * 	This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.tokenbrowser.manager;


import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;
import android.util.Pair;

import com.tokenbrowser.BuildConfig;
import com.tokenbrowser.R;
import com.tokenbrowser.crypto.HDWallet;
import com.tokenbrowser.crypto.signal.ChatService;
import com.tokenbrowser.crypto.signal.SignalPreferences;
import com.tokenbrowser.crypto.signal.model.DecryptedSignalMessage;
import com.tokenbrowser.crypto.signal.store.ProtocolStore;
import com.tokenbrowser.crypto.signal.store.SignalTrustStore;
import com.tokenbrowser.manager.model.SofaMessageTask;
import com.tokenbrowser.manager.network.IdService;
import com.tokenbrowser.manager.store.ConversationStore;
import com.tokenbrowser.manager.store.PendingMessageStore;
import com.tokenbrowser.model.local.Conversation;
import com.tokenbrowser.model.local.PendingMessage;
import com.tokenbrowser.model.local.SendState;
import com.tokenbrowser.model.local.SofaMessage;
import com.tokenbrowser.model.local.User;
import com.tokenbrowser.model.network.UserSearchResults;
import com.tokenbrowser.model.sofa.Init;
import com.tokenbrowser.model.sofa.InitRequest;
import com.tokenbrowser.model.sofa.Message;
import com.tokenbrowser.model.sofa.OutgoingAttachment;
import com.tokenbrowser.model.sofa.PaymentRequest;
import com.tokenbrowser.model.sofa.SofaAdapters;
import com.tokenbrowser.model.sofa.SofaType;
import com.tokenbrowser.service.RegistrationIntentService;
import com.tokenbrowser.util.FileNames;
import com.tokenbrowser.util.FileUtil;
import com.tokenbrowser.util.LogUtil;
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
import org.whispersystems.signalservice.api.messages.SignalServiceAttachment;
import org.whispersystems.signalservice.api.messages.SignalServiceAttachmentPointer;
import org.whispersystems.signalservice.api.messages.SignalServiceContent;
import org.whispersystems.signalservice.api.messages.SignalServiceDataMessage;
import org.whispersystems.signalservice.api.messages.SignalServiceEnvelope;
import org.whispersystems.signalservice.api.push.SignalServiceAddress;
import org.whispersystems.signalservice.internal.push.SignalServiceUrl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import rx.Completable;
import rx.Observable;
import rx.Single;
import rx.SingleSubscriber;
import rx.Subscription;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

public final class SofaMessageManager {

    private static final String ONBOARDING_BOT_NAME = "TokenBot";
    private final PublishSubject<SofaMessageTask> chatMessageQueue = PublishSubject.create();

    private SharedPreferences sharedPreferences;
    private ChatService chatService;
    private HDWallet wallet;
    private SignalTrustStore trustStore;
    private ProtocolStore protocolStore;
    private SignalServiceMessagePipe messagePipe;
    private ConversationStore conversationStore;
    private PendingMessageStore pendingMessageStore;
    private String userAgent;
    private SofaAdapters adapters;
    private boolean receiveMessages;
    private SignalServiceUrl[] signalServiceUrls;
    private String gcmToken;
    private SignalServiceMessageReceiver messageReceiver;
    private CompositeSubscription subscriptions;

    /*package*/ SofaMessageManager() {
        this.conversationStore = new ConversationStore();
        this.pendingMessageStore = new PendingMessageStore();
        this.userAgent = "Android " + BuildConfig.APPLICATION_ID + " - " + BuildConfig.VERSION_NAME +  ":" + BuildConfig.VERSION_CODE;
        this.adapters = new SofaAdapters();
        this.signalServiceUrls = new SignalServiceUrl[1];
        this.sharedPreferences = BaseApplication.get().getSharedPreferences(FileNames.GCM_PREFS, Context.MODE_PRIVATE);
        this.subscriptions = new CompositeSubscription();
    }

    public final SofaMessageManager init(final HDWallet wallet) {
        this.wallet = wallet;
        new Thread(this::initEverything).start();
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
            this.chatService.setGcmId(optional);
            this.sharedPreferences.edit().putBoolean
                    (RegistrationIntentService.CHAT_SERVICE_SENT_TOKEN_TO_SERVER, true).apply();
            this.gcmToken = null;
        } catch (IOException e) {
            this.sharedPreferences.edit().putBoolean
                    (RegistrationIntentService.CHAT_SERVICE_SENT_TOKEN_TO_SERVER, false).apply();
            LogUtil.d(getClass(), "Error during registering of GCM " + e.getMessage());
        }
    }

    public Completable tryUnregisterGcm() {
        return Completable.fromAction(() -> {
            try {
                this.chatService.setGcmId(Optional.absent());
                this.sharedPreferences.edit().putBoolean
                        (RegistrationIntentService.CHAT_SERVICE_SENT_TOKEN_TO_SERVER, false).apply();
            } catch (IOException e) {
                LogUtil.d(getClass(), "Error during unregistering of GCM " + e.getMessage());
            }
        })
        .subscribeOn(Schedulers.io());
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
                .subscribeOn(Schedulers.io());
    }

    public final Single<Conversation> loadConversation(final String conversationId) {
        return Single
                .fromCallable(() -> conversationStore.loadByAddress(conversationId))
                .subscribeOn(Schedulers.io());
    }

    public final Observable<Conversation> registerForAllConversationChanges() {
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

    public final Single<Boolean> areUnreadMessages() {
        return Single
                .fromCallable(() -> conversationStore.areUnreadMessages())
                .subscribeOn(Schedulers.io());
    }

    private void initEverything() {
        generateStores();
        initSignalMessageReceiver();
        registerIfNeeded();
        attachSubscribers();
    }

    private void generateStores() {
        this.protocolStore = new ProtocolStore().init();
        this.trustStore = new SignalTrustStore();

        final SignalServiceUrl signalServiceUrl = new SignalServiceUrl(
                BaseApplication.get().getResources().getString(R.string.chat_url),
                this.trustStore);
        this.signalServiceUrls[0] = signalServiceUrl;
        this.chatService = new ChatService(this.signalServiceUrls, this.wallet, this.protocolStore, this.userAgent);
    }

    private void initSignalMessageReceiver() {
        this.messageReceiver = new SignalServiceMessageReceiver(
                this.signalServiceUrls,
                this.wallet.getOwnerAddress(),
                this.protocolStore.getPassword(),
                this.protocolStore.getSignalingKey(),
                this.userAgent);
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
        this.chatService.registerKeys(
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
        final Subscription sub = this.chatMessageQueue
                .observeOn(Schedulers.io())
                .subscribeOn(Schedulers.io())
                .subscribe(
                        this::handleMessage,
                        this::handleMessageError);

        this.subscriptions.add(sub);

        BaseApplication
                .get()
                .isConnectedSubject()
                .filter(isConnected -> isConnected)
                .subscribe(isConnected -> sendPendingMessages());
    }

    private void handleMessage(final SofaMessageTask messageTask) {
        switch (messageTask.getAction()) {
            case SofaMessageTask.SEND_AND_SAVE:
                sendMessageToRemotePeer(messageTask, true);
                break;
            case SofaMessageTask.SAVE_ONLY:
                storeMessage(messageTask.getReceiver(), messageTask.getSofaMessage(), SendState.STATE_LOCAL_ONLY);
                break;
            case SofaMessageTask.SAVE_TRANSACTION:
                storeMessage(messageTask.getReceiver(), messageTask.getSofaMessage(), SendState.STATE_SENDING);
                break;
            case SofaMessageTask.SEND_ONLY:
                sendMessageToRemotePeer(messageTask, false);
                break;
            case SofaMessageTask.UPDATE_MESSAGE:
                updateExistingMessage(messageTask.getReceiver(), messageTask.getSofaMessage());
                break;
        }
    }

    private void handleMessageError(final Throwable throwable) {
        LogUtil.e(getClass(), "Message sending/receiving now broken due to this error: " + throwable);
    }

    private void sendPendingMessages() {
        final List<PendingMessage> pendingMessages = this.pendingMessageStore.fetchAllPendingMessages();
        for (final PendingMessage pendingMessage : pendingMessages) {
            sendAndSaveMessage(pendingMessage.getReceiver(), pendingMessage.getSofaMessage());
        }
    }

    private void sendMessageToRemotePeer(final SofaMessageTask messageTask, final boolean saveMessageToDatabase) {
        final User receiver = messageTask.getReceiver();
        final SofaMessage message = messageTask.getSofaMessage();

        if (saveMessageToDatabase) {
            this.conversationStore.saveNewMessage(receiver, message);
        }

        if (!BaseApplication.get().isConnected() && saveMessageToDatabase) {
            message.setSendState(SendState.STATE_PENDING);
            updateExistingMessage(receiver, message);
            savePendingMessage(receiver, message);
            return;
        }

        try {
            sendToSignal(messageTask);

            if (saveMessageToDatabase) {
                message.setSendState(SendState.STATE_SENT);
                updateExistingMessage(receiver, message);
            }
        } catch (final UntrustedIdentityException ue) {
            LogUtil.error(getClass(), "Keys have changed. " + ue);
            protocolStore.saveIdentity(
                    new SignalProtocolAddress(receiver.getTokenId(), SignalServiceAddress.DEFAULT_DEVICE_ID),
                    ue.getIdentityKey());
        } catch (final IOException ex) {
            LogUtil.error(getClass(), ex.toString());
            if (saveMessageToDatabase) {
                message.setSendState(SendState.STATE_FAILED);
                updateExistingMessage(receiver, message);
            }
        }
    }

    private void sendToSignal(final SofaMessageTask messageTask) throws UntrustedIdentityException, IOException {
        final SignalServiceAddress receivingAddress = new SignalServiceAddress(messageTask.getReceiver().getTokenId());
        final SignalServiceDataMessage message = buildMessage(messageTask);
        generateMessageSender().sendMessage(receivingAddress, message);
    }

    private SignalServiceMessageSender generateMessageSender() {
        return new SignalServiceMessageSender(
                this.signalServiceUrls,
                this.wallet.getOwnerAddress(),
                this.protocolStore.getPassword(),
                this.protocolStore,
                this.userAgent,
                Optional.absent(),
                Optional.absent()
        );
    }

    private SignalServiceDataMessage buildMessage(final SofaMessageTask messageTask) throws FileNotFoundException {
        final SignalServiceDataMessage.Builder messageBuilder = SignalServiceDataMessage.newBuilder();
        messageBuilder.withBody(messageTask.getSofaMessage().getAsSofaMessage());
        final OutgoingAttachment outgoingAttachment = new OutgoingAttachment(messageTask.getSofaMessage());
        if (outgoingAttachment.isValid()) {
            final SignalServiceAttachment signalAttachment = buildOutgoingAttachment(outgoingAttachment);
            messageBuilder.withAttachment(signalAttachment);
        }

        return messageBuilder.build();
    }

    private SignalServiceAttachment buildOutgoingAttachment(final OutgoingAttachment attachment) throws FileNotFoundException {
        final FileInputStream attachmentStream = new FileInputStream(attachment.getOutgoingAttachment());
        return SignalServiceAttachment.newStreamBuilder()
                .withStream(attachmentStream)
                .withContentType(attachment.getMimeType())
                .withLength(attachment.getOutgoingAttachment().length())
                .build();
    }

    private void storeMessage(final User receiver, final SofaMessage message, final @SendState.State int sendState) {
        message.setSendState(sendState);
        this.conversationStore.saveNewMessage(receiver, message);
    }

    private void updateExistingMessage(final User receiver, final SofaMessage message) {
        this.conversationStore.updateMessage(receiver, message);
    }

    private void savePendingMessage(final User receiver, final SofaMessage message) {
        this.pendingMessageStore.save(receiver, message);
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
            final Optional<List<SignalServiceAttachment>> attachments = dataMessage.get().getAttachments();
            final DecryptedSignalMessage decryptedMessage = new DecryptedSignalMessage(messageSource, messageBody.get(), attachments);
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

        processAttachments(signalMessage);

        BaseApplication
        .get()
        .getTokenManager()
        .getUserManager()
        .getUserFromAddress(signalMessage.getSource())
        .subscribe((user) -> this.saveIncomingMessageFromUserToDatabase(user, signalMessage));
    }

    private void processAttachments(final DecryptedSignalMessage signalMessage) {
        if (!signalMessage.getAttachments().isPresent()) {
            return;
        }

        final List<SignalServiceAttachment> attachments = signalMessage.getAttachments().get();
        if (attachments.size() > 0) {
            final SignalServiceAttachment attachment = attachments.get(0);
            final String filePath = saveAttachmentToFile(attachment.asPointer());
            signalMessage.setAttachmentFilePath(filePath);
        }
    }

    private @Nullable String saveAttachmentToFile(final SignalServiceAttachmentPointer attachment) {
        final FileUtil fileUtil = new FileUtil();
        final File attachmentFile = fileUtil.writeAttachmentToFileFromMessageReceiver(attachment, this.messageReceiver);
        return attachmentFile != null ? attachmentFile.getAbsolutePath() : null;
    }

    private void saveIncomingMessageFromUserToDatabase(final User user, final DecryptedSignalMessage signalMessage) {
        final SofaMessage remoteMessage = new SofaMessage()
                .makeNew(user, signalMessage.getBody())
                .setAttachmentFilePath(signalMessage.getAttachmentFilePath());
        if (remoteMessage.getType() == SofaType.PAYMENT) {
            // Don't render incoming SOFA::Payments,
            // but ensure we have the sender cached.
            fetchAndCacheIncomingPaymentSender(user);
            return;
        } else if(remoteMessage.getType() == SofaType.PAYMENT_REQUEST) {
            generatePayloadWithLocalAmountEmbedded(remoteMessage)
                    .subscribe((updatedPayload) -> {
                        remoteMessage.setPayload(updatedPayload);
                        this.conversationStore.saveNewMessage(user, remoteMessage);
                    });
            return;
        } else if (remoteMessage.getType() == SofaType.INIT_REQUEST) {
            // Don't render initRequests,
            // but respond to them.
            respondToInitRequest(user, remoteMessage);
            return;
        }

        this.conversationStore.saveNewMessage(user, remoteMessage);
    }

    private void respondToInitRequest(final User sender, final SofaMessage remoteMessage) {
        try {
            final InitRequest initRequest = this.adapters.initRequestFrom(remoteMessage.getPayload());
            final Init initMessage = new Init().construct(initRequest, this.wallet.getPaymentAddress());
            final String payload = this.adapters.toJson(initMessage);
            final SofaMessage newSofaMessage = new SofaMessage().makeNew(sender, payload);

            BaseApplication.get()
                    .getTokenManager()
                    .getSofaMessageManager()
                    .sendMessage(sender, newSofaMessage);
        } catch (final IOException e) {
            LogUtil.e(getClass(), "Failed to respond to incoming init request " + e);
        }
    }

    private void fetchAndCacheIncomingPaymentSender(final User sender) {
        BaseApplication
        .get()
        .getTokenManager()
        .getUserManager()
        .getUserFromAddress(sender.getTokenId());
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
                .subscribe(
                        this::handleOnboardingBotFound,
                        e -> LogUtil.e(getClass(), "Onboarding bot not found. " + e.toString()));
    }

    private void handleOnboardingBotFound(final UserSearchResults results) {
        final List<User> users = results.getResults();
        for (final User user : users) {
            if (user.getUsernameForEditing().equals(ONBOARDING_BOT_NAME)) {
                sendOnboardMessageToOnboardingBot(user);
                break;
            }
        }
    }

    private void sendOnboardMessageToOnboardingBot(final User onboardingBot) {
        BaseApplication
                .get()
                .getTokenManager()
                .getUserManager()
                .getCurrentUser()
                .map(this::generateOnboardingMessage)
                .doOnSuccess(__ -> SharedPrefsUtil.setHasOnboarded())
                .subscribe(onboardingMessage -> this.sendOnboardingMessage(onboardingMessage, onboardingBot));
    }

    private void sendOnboardingMessage(final SofaMessage onboardingMessage, final User onboardingBot) {
        BaseApplication
            .get()
            .getTokenManager()
            .getSofaMessageManager()
            .sendMessage(onboardingBot, onboardingMessage);
    }

    private SofaMessage generateOnboardingMessage(final User localUser) {
        final Message sofaMessage = new Message().setBody("");
        final String messageBody = new SofaAdapters().toJson(sofaMessage);
        return new SofaMessage().makeNew(localUser, messageBody);
    }

    public void clear() {
        disconnect();
        this.protocolStore.deleteAllSessions();
        clearSubscriptions();
        this.sharedPreferences
                .edit()
                .clear()
                .apply();
    }

    private void clearSubscriptions() {
        this.subscriptions.clear();
    }
}
