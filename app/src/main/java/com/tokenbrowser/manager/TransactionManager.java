package com.tokenbrowser.manager;

import com.tokenbrowser.crypto.HDWallet;
import com.tokenbrowser.exception.UnknownTransactionException;
import com.tokenbrowser.manager.model.PaymentTask;
import com.tokenbrowser.manager.network.BalanceService;
import com.tokenbrowser.manager.store.PendingTransactionStore;
import com.tokenbrowser.model.local.PendingTransaction;
import com.tokenbrowser.model.local.SendState;
import com.tokenbrowser.model.local.SofaMessage;
import com.tokenbrowser.model.local.User;
import com.tokenbrowser.model.network.SentTransaction;
import com.tokenbrowser.model.network.ServerTime;
import com.tokenbrowser.model.network.SignedTransaction;
import com.tokenbrowser.model.network.TransactionRequest;
import com.tokenbrowser.model.network.UnsignedTransaction;
import com.tokenbrowser.model.sofa.Payment;
import com.tokenbrowser.model.sofa.PaymentRequest;
import com.tokenbrowser.model.sofa.SofaAdapters;
import com.tokenbrowser.token.R;
import com.tokenbrowser.util.LocaleUtil;
import com.tokenbrowser.util.LogUtil;
import com.tokenbrowser.util.OnNextSubscriber;
import com.tokenbrowser.view.BaseApplication;
import com.tokenbrowser.view.notification.ChatNotificationManager;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import rx.Observable;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

import static com.tokenbrowser.manager.model.PaymentTask.INCOMING;
import static com.tokenbrowser.manager.model.PaymentTask.OUTGOING;

public class TransactionManager {

    private final PublishSubject<PaymentTask> newPaymentQueue = PublishSubject.create();
    private final PublishSubject<Payment> updatePaymentQueue = PublishSubject.create();

    private HDWallet wallet;
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
                                                final SofaMessage sofaMessage,
                                                final @PaymentRequest.State int newState) {
        this.dbThreadExecutor.submit(() -> {
            try {
                final PaymentRequest paymentRequest = adapters
                        .txRequestFrom(sofaMessage.getPayload())
                        .setState(newState);

                final String updatedPayload = adapters.toJson(paymentRequest);
                sofaMessage.setPayload(updatedPayload);
                BaseApplication
                        .get()
                        .getTokenManager()
                        .getSofaMessageManager()
                        .updateMessage(remoteUser, sofaMessage);

                if (newState == PaymentRequest.ACCEPTED) {
                    sendPayment(remoteUser, paymentRequest.getValue());
                }

            } catch (final IOException ex) {
                LogUtil.e(getClass(), "Error changing Payment Request state. " + ex);
            }
        });
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

    private void createNewPayment(final User sender, final Payment payment) {
        final PaymentTask task = new PaymentTask(
                sender,
                payment,
                payment.getFromAddress().equals(wallet.getPaymentAddress()) ? OUTGOING : INCOMING);
        this.newPaymentQueue.onNext(task);
    }

    private void processNewPayment(final PaymentTask task) {
        final Payment payment = task.getPayment();
        final User user = task.getUser();

        payment.generateLocalPrice();

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
                        showPaymentFailedNotification(receiver);
                        unsubscribe();
                    }

                    private void showPaymentFailedNotification(final User receiver) {
                        final String title = BaseApplication.get().getString(R.string.payment_failed);
                        final String content = String.format(
                                LocaleUtil.getLocale(),
                                BaseApplication.get().getString(R.string.payment_failed_message),
                                receiver.getDisplayName());
                        ChatNotificationManager.showNotification(title, content, receiver.getOwnerAddress());
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
        BaseApplication
                .get()
                .getTokenManager()
                .getSofaMessageManager()
                .updateMessage(user, message);
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
        BaseApplication
                .get()
                .getTokenManager()
                .getSofaMessageManager()
                .saveTransaction(user, message);
    }

    private void storeUnconfirmedTransaction(final String txHash, final SofaMessage message) {
        final PendingTransaction pendingTransaction = new PendingTransaction()
                                                            .setSofaMessage(message)
                                                            .setTxHash(txHash);
        this.pendingTransactionStore.save(pendingTransaction);
    }

    private void updatePendingTransaction(final PendingTransaction pendingTransaction, final Payment updatedPayment) {

        if (pendingTransaction == null) {
            // Never seen this transaction before so process it as a new transaction
            BaseApplication
            .get()
            .getTokenManager()
            .getUserManager()
            .getUserFromPaymentAddress(updatedPayment.getFromAddress())
            .subscribe((sender) -> createNewPayment(sender, updatedPayment));
            return;
        }

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

        final SofaMessage sofaMessage = pendingTransaction.getSofaMessage();
        final Payment existingPayment = adapters.paymentFrom(sofaMessage.getPayload());

        existingPayment.setStatus(updatedPayment.getStatus());

        final String messageBody = adapters.toJson(existingPayment);
        return sofaMessage.setPayload(messageBody);
    }
}