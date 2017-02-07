package com.bakkenbaeck.token.manager;


import android.support.annotation.NonNull;

import com.bakkenbaeck.token.crypto.HDWallet;
import com.bakkenbaeck.token.model.local.ChatMessage;
import com.bakkenbaeck.token.model.local.PendingTransaction;
import com.bakkenbaeck.token.model.local.SendState;
import com.bakkenbaeck.token.model.network.SentTransaction;
import com.bakkenbaeck.token.model.network.ServerTime;
import com.bakkenbaeck.token.model.network.SignedTransaction;
import com.bakkenbaeck.token.model.network.TransactionRequest;
import com.bakkenbaeck.token.model.network.UnsignedTransaction;
import com.bakkenbaeck.token.model.sofa.Payment;
import com.bakkenbaeck.token.model.sofa.SofaAdapters;
import com.bakkenbaeck.token.network.BalanceService;
import com.bakkenbaeck.token.presenter.store.ChatMessageStore;
import com.bakkenbaeck.token.presenter.store.PendingTransactionStore;
import com.bakkenbaeck.token.util.LogUtil;
import com.bakkenbaeck.token.util.OnNextSubscriber;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import rx.Observable;
import rx.SingleSubscriber;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

public class TransactionManager {

    private final PublishSubject<Payment> newPaymentQueue = PublishSubject.create();
    private final PublishSubject<Payment> updatePaymentQueue = PublishSubject.create();

    private HDWallet wallet;
    private ChatMessageStore chatMessageStore;
    private PendingTransactionStore pendingTransactionStore;
    private ExecutorService dbThreadExecutor;
    private SofaAdapters adapters;

    public TransactionManager init(final HDWallet wallet) {
        this.wallet = wallet;
        new Thread(this::initEverything).start();
        return this;
    }

    public final void sendPayment(final Payment payment) {
        this.newPaymentQueue.onNext(payment);
    }

    public final void updatePayment(final Payment payment) {
        this.updatePaymentQueue.onNext(payment);
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
            TransactionManager.this.chatMessageStore = new ChatMessageStore();
            TransactionManager.this.pendingTransactionStore = new PendingTransactionStore();
        });
    }

    private void attachSubscribers() {
        attachNewPaymentSubscriber();
        attachUpdatePaymentSubscriber();
    }


    private void attachNewPaymentSubscriber() {
        this.newPaymentQueue
            .observeOn(Schedulers.io())
            .subscribeOn(Schedulers.io())
            .subscribe(this::processNewPayment);
    }

    private void processNewPayment(final Payment payment) {

        final ChatMessage storedChatMessage = storePayment(payment);

        sendNewTransaction(payment)
        .subscribe(new OnNextSubscriber<SentTransaction>() {
            @Override
            public void onError(final Throwable error) {
                LogUtil.e(getClass(), "Error creating transaction: " + error);
                updateMessageState(storedChatMessage, SendState.STATE_FAILED);
                unsubscribe();
            }

            @Override
            public void onNext(final SentTransaction sentTransaction) {
                final String txHash = sentTransaction.getTxHash();
                payment.setTxHash(txHash);

                final ChatMessage updatedMessage = generateMessageFromPayment(payment);
                storedChatMessage.setPayload(updatedMessage.getPayload());
                updateMessageState(storedChatMessage, SendState.STATE_SENT);
                storeUnconfirmedTransaction(txHash, storedChatMessage);
                unsubscribe();
            }
        });
    }

    private ChatMessage storePayment(final Payment payment) {
        final ChatMessage chatMessage = generateMessageFromPayment(payment);
        storeMessage(chatMessage);
        return chatMessage;
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
                .setFromAddress(this.wallet.getPaymentAddress())
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


    // -----------------------------------------
    // -----------------------------------------
    // -----------------------------------------
    // -----------------------------------------
    // -----------------------------------------
    // -----------------------------------------
    // -----------------------------------------
    // -----------------------------------------



    private void attachUpdatePaymentSubscriber() {
        this.updatePaymentQueue
            .observeOn(Schedulers.io())
            .subscribeOn(Schedulers.io())
            .subscribe(new OnNextSubscriber<Payment>() {
                @Override
                public void onNext(final Payment payment) {
                    final String txHash = payment.getTxHash();
                    final ChatMessage chatMessage = generateMessageFromPayment(payment);
                    updateUnconfirmedTransaction(txHash, chatMessage);
                }
            });
    }

    private ChatMessage generateMessageFromPayment(final Payment payment) {
        final String messageBody = this.adapters.toJson(payment);
        return new ChatMessage().makeNew(payment.getOwnerAddress(), true, messageBody);
    }

    private void storeMessage(final ChatMessage message) {
        dbThreadExecutor.execute(new Runnable() {
            @Override
            public void run() {
                message.setSendState(SendState.STATE_SENDING);
                chatMessageStore.save(message);
            }
        });
    }

    private void updateMessageState(final ChatMessage message, final @SendState.State int sendState) {
        message.setSendState(sendState);
        updateMessage(message);
    }

    private void updateMessage(final ChatMessage message) {
        dbThreadExecutor.execute(new Runnable() {
            @Override
            public void run() {
                chatMessageStore.update(message);
            }
        });
    }

    private void storeUnconfirmedTransaction(final String txHash, final ChatMessage message) {
        dbThreadExecutor.execute(new Runnable() {
            @Override
            public void run() {
                final PendingTransaction pendingTransaction =
                        new PendingTransaction()
                                .setChatMessage(message)
                                .setTxHash(txHash);

                pendingTransactionStore.save(pendingTransaction);
            }
        });
    }

    private void updateUnconfirmedTransaction(final String txHash, final ChatMessage newMessage) {
        dbThreadExecutor.execute(new Runnable() {
            @Override
            public void run() {
                pendingTransactionStore
                    .load(txHash)
                    .subscribe(new SingleSubscriber<PendingTransaction>() {
                        @Override
                        public void onSuccess(final PendingTransaction pendingTransaction) {
                            try {
                                final ChatMessage updatedMessage = updateStatusFromPendingTransaction(pendingTransaction, newMessage);
                                final PendingTransaction updatedPendingTransaction = new PendingTransaction()
                                        .setTxHash(txHash)
                                        .setChatMessage(updatedMessage);

                                pendingTransactionStore.save(updatedPendingTransaction);
                                unsubscribe();
                            } catch (final IOException ex) {
                                LogUtil.e(getClass(), "Error updating PendingTransaction. " + ex);
                            }
                        }

                        @Override
                        public void onError(final Throwable error) {
                            storeUnconfirmedTransaction(txHash, newMessage);
                            unsubscribe();
                        }
                    });
            }

            @NonNull
            private ChatMessage updateStatusFromPendingTransaction(final PendingTransaction pendingTransaction, final ChatMessage newMessage) throws IOException {
                final ChatMessage existingMessage = pendingTransaction.getChatMessage();
                final Payment existingPayment = adapters.paymentFrom(existingMessage.getPayload());

                final Payment newPayment = adapters.paymentFrom(newMessage.getPayload());
                existingPayment.setStatus(newPayment.getStatus());

                final String messageBody = adapters.toJson(existingPayment);

                final ChatMessage updatedMessage = new ChatMessage(existingMessage);
                return updatedMessage.setPayload(messageBody);
            }
        });
    }
}