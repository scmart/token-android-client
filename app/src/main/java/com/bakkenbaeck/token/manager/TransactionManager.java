package com.bakkenbaeck.token.manager;

import com.bakkenbaeck.token.crypto.HDWallet;
import com.bakkenbaeck.token.exception.UnknownTransactionException;
import com.bakkenbaeck.token.manager.model.PaymentTask;
import com.bakkenbaeck.token.manager.network.BalanceService;
import com.bakkenbaeck.token.manager.store.ConversationStore;
import com.bakkenbaeck.token.manager.store.PendingTransactionStore;
import com.bakkenbaeck.token.model.local.PendingTransaction;
import com.bakkenbaeck.token.model.local.SendState;
import com.bakkenbaeck.token.model.local.SofaMessage;
import com.bakkenbaeck.token.model.local.User;
import com.bakkenbaeck.token.model.network.SentTransaction;
import com.bakkenbaeck.token.model.network.ServerTime;
import com.bakkenbaeck.token.model.network.SignedTransaction;
import com.bakkenbaeck.token.model.network.TransactionRequest;
import com.bakkenbaeck.token.model.network.UnsignedTransaction;
import com.bakkenbaeck.token.model.sofa.Payment;
import com.bakkenbaeck.token.model.sofa.PaymentRequest;
import com.bakkenbaeck.token.model.sofa.SofaAdapters;
import com.bakkenbaeck.token.util.LogUtil;
import com.bakkenbaeck.token.util.OnNextSubscriber;
import com.bakkenbaeck.token.view.BaseApplication;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import rx.Observable;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

import static com.bakkenbaeck.token.manager.model.PaymentTask.INCOMING;
import static com.bakkenbaeck.token.manager.model.PaymentTask.OUTGOING;

public class TransactionManager {

    private final PublishSubject<PaymentTask> newPaymentQueue = PublishSubject.create();
    private final PublishSubject<Payment> updatePaymentQueue = PublishSubject.create();

    private HDWallet wallet;
    private ConversationStore conversationStore;
    private PendingTransactionStore pendingTransactionStore;
    private ExecutorService dbThreadExecutor;
    private SofaAdapters adapters;

    public TransactionManager init(final HDWallet wallet) {
        this.wallet = wallet;
        new Thread(this::initEverything).start();
        return this;
    }

    public PublishSubject<PendingTransaction> getPendingTransactionObservable() {
        return this.pendingTransactionStore.getPendingTransactionObservable();
    }

    public final void sendPayment(final User receiver, final String amount) {
        final Payment payment = new Payment()
                .setValue(amount)
                .setFromAddress(this.wallet.getPaymentAddress())
                .setToAddress(receiver.getPaymentAddress());

        final PaymentTask task = new PaymentTask(receiver, payment, OUTGOING);
        this.newPaymentQueue.onNext(task);
    }

    public final void updatePayment(final Payment payment) {
        this.updatePaymentQueue.onNext(payment);
    }

    public final void updatePaymentRequestState(final User remoteUser,
                                                final SofaMessage existingMessage,
                                                final @PaymentRequest.State int newState) {
        try {
            final PaymentRequest paymentRequest = adapters
                    .txRequestFrom(existingMessage.getPayload())
                    .setState(newState);

            final String updatedPayload = adapters.toJson(paymentRequest);
            final SofaMessage updatedMessage = new SofaMessage(existingMessage).setPayload(updatedPayload);
            conversationStore.updateMessage(remoteUser, updatedMessage);

            if (newState == PaymentRequest.ACCEPTED) {
                sendPayment(remoteUser, paymentRequest.getValue());
            }

        } catch (final IOException ex) {
            LogUtil.e(getClass(), "Error changing Payment Request state. " + ex);
        }
    }

    public final void processIncomingPayment(final User sender, final Payment payment) {
        final PaymentTask task = new PaymentTask(sender, payment, PaymentTask.INCOMING);
        this.newPaymentQueue.onNext(task);
    }

    private void initEverything() {
        initAdapters();
        initDatabase();
        attachSubscribers();
    }

    private void initAdapters() {
        this.adapters = new SofaAdapters();
    }

    private void initDatabase() {
        this.dbThreadExecutor = Executors.newSingleThreadExecutor();
        this.dbThreadExecutor.submit(() -> {
            TransactionManager.this.conversationStore = new ConversationStore();
            TransactionManager.this.pendingTransactionStore = new PendingTransactionStore();
        });
    }

    private void attachSubscribers() {
        attachNewPaymentSubscriber();
        attachUpdatePaymentSubscriber();
    }


    private void attachNewPaymentSubscriber() {
        this.newPaymentQueue
            .observeOn(Schedulers.from(dbThreadExecutor))
            .subscribeOn(Schedulers.from(dbThreadExecutor))
            .subscribe(this::processNewPayment);
    }

    private void processNewPayment(final PaymentTask task) {
        final Payment payment = task.getPayment();
        final User user = task.getUser();

        switch (task.getAction()) {
            case INCOMING: {
                final SofaMessage storedSofaMessage = storePayment(task.getUser(), payment, false);
                handleIncomingPayment(payment, storedSofaMessage);
                break;
            }
            case OUTGOING: {
                final SofaMessage storedSofaMessage = storePayment(task.getUser(), payment, true);
                handleOutgoingPayment(user, payment, storedSofaMessage);
                break;
            }
        }
    }

    private SofaMessage storePayment(final User user, final Payment payment, final boolean sentByLocal) {
        final SofaMessage sofaMessage = generateMessageFromPayment(payment, sentByLocal);
        storeMessage(user, sofaMessage);
        return sofaMessage;
    }

    private void handleIncomingPayment(final Payment payment, final SofaMessage storedSofaMessage) {
        final PendingTransaction pendingTransaction =
                new PendingTransaction()
                        .setTxHash(payment.getTxHash())
                        .setSofaMessage(storedSofaMessage);
        this.pendingTransactionStore.save(pendingTransaction);
    }

    private void handleOutgoingPayment(final User receiver, final Payment payment, final SofaMessage storedSofaMessage) {
        sendNewTransaction(payment)
                .observeOn(Schedulers.from(dbThreadExecutor))
                .subscribeOn(Schedulers.from(dbThreadExecutor))
                .subscribe(new OnNextSubscriber<SentTransaction>() {
                    @Override
                    public void onError(final Throwable error) {
                        LogUtil.e(getClass(), "Error creating transaction: " + error);
                        updateMessageState(receiver, storedSofaMessage, SendState.STATE_FAILED);
                        unsubscribe();
                    }

                    @Override
                    public void onNext(final SentTransaction sentTransaction) {
                        final String txHash = sentTransaction.getTxHash();
                        payment.setTxHash(txHash);

                        // Update the stored message with the transactions details
                        final SofaMessage updatedMessage = generateMessageFromPayment(payment, true);
                        storedSofaMessage.setPayload(updatedMessage.getPayloadWithHeaders());
                        updateMessageState(receiver, storedSofaMessage, SendState.STATE_SENT);
                        storeUnconfirmedTransaction(txHash, storedSofaMessage);

                        BaseApplication
                                .get()
                                .getTokenManager()
                                .getSofaMessageManager()
                                .sendMessage(receiver, storedSofaMessage);
                        unsubscribe();
                    }
                });
    }

    private Observable<SentTransaction> sendNewTransaction(final Payment payment) {
        final TransactionRequest transactionRequest = generateTransactionRequest(payment);
        return BalanceService.getApi()
                .createTransaction(transactionRequest)
                .toObservable()
                .switchMap(this::signAndSendTransaction);
    }

    private TransactionRequest generateTransactionRequest(final Payment payment) {
        return new TransactionRequest()
                .setValue(payment.getValue())
                .setFromAddress(payment.getFromAddress())
                .setToAddress(payment.getToAddress());
    }

    private Observable<SentTransaction> signAndSendTransaction(final UnsignedTransaction unsignedTransaction) {
        return BalanceService
                .getApi()
                .getTimestamp()
                .flatMapObservable(st -> signAndSendTransactionWithTimestamp(unsignedTransaction, st));
    }

    private Observable<SentTransaction> signAndSendTransactionWithTimestamp(final UnsignedTransaction unsignedTransaction, final ServerTime serverTime) {
        final String signature = this.wallet.signTransaction(unsignedTransaction.getTransaction());
        final SignedTransaction signedTransaction = new SignedTransaction()
                .setEncodedTransaction(unsignedTransaction.getTransaction())
                .setSignature(signature);

        final long timestamp = serverTime.get();

        return BalanceService.getApi()
                .sendSignedTransaction(timestamp, signedTransaction)
                .toObservable();
    }

    private void updateMessageState(final User user, final SofaMessage message, final @SendState.State int sendState) {
        message.setSendState(sendState);
        updateMessage(user, message);
    }

    private void updateMessage(final User user, final SofaMessage message) {
        this.conversationStore.updateMessage(user, message);
    }


    private SofaMessage generateMessageFromPayment(final Payment payment, final boolean sentByLocal) {
        final String messageBody = this.adapters.toJson(payment);
        return new SofaMessage().makeNew(sentByLocal, messageBody);
    }


    private void attachUpdatePaymentSubscriber() {
        this.updatePaymentQueue
            .observeOn(Schedulers.io())
            .subscribeOn(Schedulers.io())
            .subscribe(this::processUpdatedPayment);
    }

    private void processUpdatedPayment(final Payment payment) {
        pendingTransactionStore
                .load(payment.getTxHash())
                .subscribeOn(Schedulers.from(dbThreadExecutor))
                .observeOn(Schedulers.from(dbThreadExecutor))
                .toObservable()
                .subscribe(pendingTransaction -> updatePendingTransaction(pendingTransaction, payment));
    }

    private void storeMessage(final User user, final SofaMessage message) {
        message.setSendState(SendState.STATE_SENDING);
        this.conversationStore.saveNewMessage(user, message);
    }

    private void storeUnconfirmedTransaction(final String txHash, final SofaMessage message) {
        final PendingTransaction pendingTransaction = new PendingTransaction()
                                                            .setSofaMessage(message)
                                                            .setTxHash(txHash);
        this.pendingTransactionStore.save(pendingTransaction);
    }

    private void updatePendingTransaction(final PendingTransaction pendingTransaction, final Payment updatedPayment) {

        final SofaMessage updatedMessage;
        try {
            updatedMessage = updateStatusFromPendingTransaction(pendingTransaction, updatedPayment);
        } catch (final IOException | UnknownTransactionException ex) {
            LogUtil.e(getClass(), "Unable to update pending transaction. " + ex.getMessage());
            return;
        }

        final PendingTransaction updatedPendingTransaction = new PendingTransaction()
                .setTxHash(pendingTransaction.getTxHash())
                .setSofaMessage(updatedMessage);

        this.pendingTransactionStore.save(updatedPendingTransaction);
    }

    private SofaMessage updateStatusFromPendingTransaction(final PendingTransaction pendingTransaction, final Payment updatedPayment) throws IOException, UnknownTransactionException {
        if (pendingTransaction == null) {
            throw new UnknownTransactionException("PendingTransaction could not be found. This transaction probably came from outside of Token.");
        }

        final SofaMessage existingMessage = pendingTransaction.getSofaMessage();
        final Payment existingPayment = adapters.paymentFrom(existingMessage.getPayload());

        existingPayment.setStatus(updatedPayment.getStatus());

        final String messageBody = adapters.toJson(existingPayment);

        final SofaMessage updatedMessage = new SofaMessage(existingMessage);
        return updatedMessage.setPayload(messageBody);
    }
}