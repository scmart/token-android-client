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

import android.support.annotation.NonNull;
import android.util.Pair;

import com.tokenbrowser.R;
import com.tokenbrowser.crypto.HDWallet;
import com.tokenbrowser.exception.UnknownTransactionException;
import com.tokenbrowser.manager.model.PaymentTask;
import com.tokenbrowser.manager.network.EthereumService;
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
import com.tokenbrowser.model.sofa.SofaType;
import com.tokenbrowser.util.LocaleUtil;
import com.tokenbrowser.util.LogUtil;
import com.tokenbrowser.util.OnNextSubscriber;
import com.tokenbrowser.view.BaseApplication;
import com.tokenbrowser.view.notification.ChatNotificationManager;

import java.io.IOException;

import rx.Observable;
import rx.Subscription;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

import static com.tokenbrowser.manager.model.PaymentTask.INCOMING;
import static com.tokenbrowser.manager.model.PaymentTask.OUTGOING;

public class TransactionManager {

    private final PublishSubject<PaymentTask> newPaymentQueue = PublishSubject.create();
    private final PublishSubject<Payment> updatePaymentQueue = PublishSubject.create();

    private HDWallet wallet;
    private PendingTransactionStore pendingTransactionStore;
    private SofaAdapters adapters;
    private CompositeSubscription subscriptions;

    /*package */ TransactionManager() {
        initDatabase();
        initSubscriptions();
    }

    public TransactionManager init(final HDWallet wallet) {
        this.wallet = wallet;
        new Thread(this::initEverything).start();
        return this;
    }

    public PublishSubject<PendingTransaction> getPendingTransactionObservable() {
        return this.pendingTransactionStore.getPendingTransactionObservable();
    }

    public Observable<PendingTransaction> getAllTransactions() {
        return this.pendingTransactionStore.loadAllTransactions();
    }

    public final void sendPayment(final User receiver, final String amount) {
        sendPayment(receiver, receiver.getPaymentAddress(), amount);
    }

    private void sendPayment(final User receiver, final String paymentAddress, final String amount) {
        new Payment()
            .setValue(amount)
            .setFromAddress(this.wallet.getPaymentAddress())
            .setToAddress(paymentAddress)
            .generateLocalPrice()
            .subscribe((payment -> {
                final PaymentTask task = new PaymentTask(receiver, payment, OUTGOING);
                this.newPaymentQueue.onNext(task);
            }));
    }

    public final void updatePayment(final Payment payment) {
        this.updatePaymentQueue.onNext(payment);
    }

    public final void updatePaymentRequestState(final User remoteUser,
                                                final SofaMessage sofaMessage,
                                                final @PaymentRequest.State int newState) {
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
                sendPayment(remoteUser, paymentRequest.getDestinationAddresss(), paymentRequest.getValue());
            }

        } catch (final IOException ex) {
            LogUtil.e(getClass(), "Error changing Payment Request state. " + ex);
        }
    }

    private void initEverything() {
        initAdapters();
        updatePendingTransactions();
        attachSubscribers();
    }

    private void initAdapters() {
        this.adapters = new SofaAdapters();
    }

    private void updatePendingTransactions() {
        final Subscription sub = this.pendingTransactionStore
                .loadAllTransactions()
                .filter(this::isUnconfirmed)
                .flatMap(pendingTransaction -> Observable.zip(
                        Observable.just(pendingTransaction),
                        getTransactionStatus(pendingTransaction),
                        Pair::new
                ))
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .onErrorReturn(__ -> new Pair<>(null, null))
                .subscribe(pair -> this.updatePendingTransaction(pair.first, pair.second));

        this.subscriptions.add(sub);
    }

    @NonNull
    private Observable<Payment> getTransactionStatus(final PendingTransaction pendingTransaction) {
        return BaseApplication
                .get()
                .getTokenManager()
                .getBalanceManager()
                .getTransactionStatus(pendingTransaction.getTxHash())
                .toObservable();
    }

    private Boolean isUnconfirmed(final PendingTransaction pendingTransaction) {
        try {
            final SofaMessage sofaMessage = pendingTransaction.getSofaMessage();
            final Payment payment = this.adapters.paymentFrom(sofaMessage.getPayload());
            return payment.getStatus() == null || payment.getStatus().equals(SofaType.UNCONFIRMED);
        } catch (final IOException ex) {
            return false;
        }
    }

    private void initDatabase() {
        this.pendingTransactionStore = new PendingTransactionStore();
    }

    private void initSubscriptions() {
        this.subscriptions = new CompositeSubscription();
    }

    private void attachSubscribers() {
        attachNewPaymentSubscriber();
        attachUpdatePaymentSubscriber();
    }


    private void attachNewPaymentSubscriber() {
        final Subscription sub = this.newPaymentQueue
            .observeOn(Schedulers.io())
            .subscribeOn(Schedulers.io())
            .filter(paymentTask -> paymentTask.getUser() != null)
            .subscribe(this::processNewPayment);

        this.subscriptions.add(sub);
    }

    private void createNewPayment(final Payment payment) {
        BaseApplication
                .get()
                .getTokenManager()
                .getUserManager()
                .getUserFromPaymentAddress(payment.getFromAddress())
                .subscribe((sender) -> createNewPayment(sender, payment));
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

        payment
        .generateLocalPrice()
        .observeOn(Schedulers.io())
        .subscribe((updatedPayment) -> {
            switch (task.getAction()) {
                case INCOMING: {
                    final User sender = task.getUser();
                    final SofaMessage storedSofaMessage = storePayment(sender, updatedPayment, sender);
                    handleIncomingPayment(updatedPayment, storedSofaMessage);
                    break;
                }
                case OUTGOING: {
                    final User receiver = task.getUser();
                    final User sender = getCurrentLocalUser();
                    final SofaMessage storedSofaMessage = storePayment(receiver, updatedPayment, sender);
                    handleOutgoingPayment(user, updatedPayment, storedSofaMessage);
                    break;
                }
            }
        });
    }

    private SofaMessage storePayment(final User receiver, final Payment payment, final User sender) {
        final SofaMessage sofaMessage = generateMessageFromPayment(payment, sender);
        storeMessage(receiver, sofaMessage);
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
                .observeOn(Schedulers.io())
                .subscribeOn(Schedulers.io())
                .subscribe(new OnNextSubscriber<SentTransaction>() {
                    @Override
                    public void onError(final Throwable error) {
                        LogUtil.e(getClass(), "Error creating transaction: " + error);
                        updateMessageState(receiver, storedSofaMessage, SendState.STATE_FAILED);
                        showPaymentFailedNotification(receiver);
                        unsubscribe();
                    }

                    private void showPaymentFailedNotification(final User receiver) {
                        final String content = String.format(
                                LocaleUtil.getLocale(),
                                BaseApplication.get().getString(R.string.payment_failed_message),
                                receiver.getDisplayName());
                        ChatNotificationManager.showChatNotification(receiver, content);
                    }

                    @Override
                    public void onNext(final SentTransaction sentTransaction) {
                        final String txHash = sentTransaction.getTxHash();
                        payment.setTxHash(txHash);

                        // Update the stored message with the transactions details
                        final SofaMessage updatedMessage = generateMessageFromPayment(payment, getCurrentLocalUser());
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
        return EthereumService.getApi()
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
        return EthereumService
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

        return EthereumService.getApi()
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


    private SofaMessage generateMessageFromPayment(final Payment payment, final User sender) {
        final String messageBody = this.adapters.toJson(payment);
        return new SofaMessage().makeNewFromTransaction(payment.getTxHash(), sender, messageBody);
    }


    private void attachUpdatePaymentSubscriber() {
        final Subscription sub = this.updatePaymentQueue
            .observeOn(Schedulers.io())
            .subscribeOn(Schedulers.io())
            .filter(payment -> payment != null)
            .subscribe(this::processUpdatedPayment);

        this.subscriptions.add(sub);
    }

    private void processUpdatedPayment(final Payment payment) {
        pendingTransactionStore
                .loadTransaction(payment.getTxHash())
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .toObservable()
                .map(pendingTransaction -> updatePendingTransaction(pendingTransaction, payment))
                .filter(isExistingTransaction -> !isExistingTransaction)
                // This transaction has never been seen before; create and broadcast it.
                .subscribe(__ -> createNewPayment(payment));
    }

    private void storeMessage(final User receiver, final SofaMessage message) {
        message.setSendState(SendState.STATE_SENDING);
        BaseApplication
                .get()
                .getTokenManager()
                .getSofaMessageManager()
                .saveTransaction(receiver, message);
    }

    private void storeUnconfirmedTransaction(final String txHash, final SofaMessage message) {
        final PendingTransaction pendingTransaction = new PendingTransaction()
                                                            .setSofaMessage(message)
                                                            .setTxHash(txHash);
        this.pendingTransactionStore.save(pendingTransaction);
    }

    // Returns false if this is a new transaction that the app is unaware of.
    // Returns true if the transaction was correctly updated.
    private boolean updatePendingTransaction(final PendingTransaction pendingTransaction, final Payment updatedPayment) {
        if (pendingTransaction == null) {
            return false;
        }

        final SofaMessage updatedMessage;
        try {
            updatedMessage = updateStatusFromPendingTransaction(pendingTransaction, updatedPayment);
        } catch (final IOException | UnknownTransactionException ex) {
            LogUtil.e(getClass(), "Unable to update pending transaction. " + ex.getMessage());
            return false;
        }

        final PendingTransaction updatedPendingTransaction = new PendingTransaction()
                .setTxHash(pendingTransaction.getTxHash())
                .setSofaMessage(updatedMessage);

        this.pendingTransactionStore.save(updatedPendingTransaction);
        return true;
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

    private User getCurrentLocalUser() {
        // Yes, this blocks. But realistically, a value should be always ready for returning.
        return BaseApplication
                .get()
                .getTokenManager()
                .getUserManager()
                .getCurrentUser()
                .toBlocking()
                .value();
    }

    public void clear() {
        this.subscriptions.clear();
    }
}