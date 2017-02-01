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
import com.bakkenbaeck.token.presenter.store.PendingTransactionsStore;
import com.bakkenbaeck.token.util.LogUtil;
import com.bakkenbaeck.token.util.OnNextSubscriber;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import rx.SingleSubscriber;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

public class TransactionManager {

    private final PublishSubject<Payment> newPaymentQueue = PublishSubject.create();
    private final PublishSubject<Payment> updatePaymentQueue = PublishSubject.create();

    private HDWallet wallet;
    private ChatMessageStore chatMessageStore;
    private PendingTransactionsStore pendingTransactionsStore;
    private ExecutorService dbThreadExecutor;
    private SofaAdapters adapters;

    public TransactionManager init(final HDWallet wallet) {
        this.wallet = wallet;
        new Thread(new Runnable() {
            @Override
            public void run() {
                initEverything();
            }
        }).start();
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
        this.dbThreadExecutor.submit(new Runnable() {
            @Override
            public void run() {
                TransactionManager.this.chatMessageStore = new ChatMessageStore();
                TransactionManager.this.pendingTransactionsStore = new PendingTransactionsStore();
            }
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
            .subscribe(new OnNextSubscriber<Payment>() {
                @Override
                public void onNext(final Payment payment) {
                    processNewPayment(payment);
                }

                private void processNewPayment(final Payment payment) {
                    final ChatMessage chatMessage = generateMessageFromPayment(payment);
                    storeMessage(chatMessage);
                    final TransactionRequest transactionRequest = createTransactionRequest(payment);
                    processTransactionRequest(
                            transactionRequest,
                            chatMessage,
                            new SingleSubscriber<String>() {
                                @Override
                                public void onSuccess(final String txHash) {
                                    payment.setTxHash(txHash);
                                    final ChatMessage updatedMessage = generateMessageFromPayment(payment);
                                    chatMessage.setPayload(updatedMessage.getPayload());
                                    updateMessageState(chatMessage, SendState.STATE_SENT);
                                    storeUnconfirmedTransaction(txHash, chatMessage);
                                }

                                @Override
                                public void onError(final Throwable error) {
                                    LogUtil.e(getClass(), "Error creating transaction: " + error);
                                    updateMessageState(chatMessage, SendState.STATE_FAILED);
                                }
                            }
                    );
                }
            });
    }

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

                pendingTransactionsStore.save(pendingTransaction);
            }
        });
    }

    private void updateUnconfirmedTransaction(final String txHash, final ChatMessage newMessage) {
        dbThreadExecutor.execute(new Runnable() {
            @Override
            public void run() {
                pendingTransactionsStore
                    .load(txHash)
                    .subscribe(new SingleSubscriber<PendingTransaction>() {
                        @Override
                        public void onSuccess(final PendingTransaction pendingTransaction) {
                            try {
                                final ChatMessage updatedMessage = updateStatusFromPendingTransaction(pendingTransaction, newMessage);
                                final PendingTransaction updatedPendingTransaction = new PendingTransaction()
                                        .setTxHash(txHash)
                                        .setChatMessage(updatedMessage);

                                pendingTransactionsStore.save(updatedPendingTransaction);
                            } catch (final IOException ex) {
                                LogUtil.e(getClass(), "Error updating PendingTransaction. " + ex);
                            }
                        }

                        @Override
                        public void onError(final Throwable error) {
                            storeUnconfirmedTransaction(txHash, newMessage);
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

    private void processTransactionRequest(
            final TransactionRequest transactionRequest,
            final ChatMessage chatMessage,
            final SingleSubscriber<String> callback) {

        BalanceService.getApi()
                .createTransaction(transactionRequest)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(new SingleSubscriber<UnsignedTransaction>() {
                    @Override
                    public void onSuccess(final UnsignedTransaction unsignedTransaction) {
                        fetchServerTimeForUnsignedTransaction(
                                unsignedTransaction,
                                chatMessage,
                                callback);
                    }

                    @Override
                    public void onError(final Throwable error) {
                        callback.onError(error);
                    }
                });
    }

    private TransactionRequest createTransactionRequest(final Payment payment) {
        return new TransactionRequest()
                .setValue(payment.getValue())
                .setFromAddress(this.wallet.getPaymentAddress())
                .setToAddress(payment.getToAddress());
    }

    private void fetchServerTimeForUnsignedTransaction(
            final UnsignedTransaction unsignedTransaction,
            final ChatMessage chatMessage,
            final SingleSubscriber<String> callback) {

        BalanceService
                .getApi()
                .getTimestamp()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(new SingleSubscriber<ServerTime>() {
                    @Override
                    public void onSuccess(final ServerTime serverTime) {
                        signTransactionWithTimeStamp(
                                unsignedTransaction,
                                chatMessage,
                                serverTime.get(),
                                callback);
                    }

                    @Override
                    public void onError(final Throwable error) {
                        callback.onError(error);
                    }
                });
    }

    private void signTransactionWithTimeStamp(
            final UnsignedTransaction unsignedTransaction,
            final ChatMessage chatMessage,
            final long timestamp,
            final SingleSubscriber<String> callback) {

        final String signature = wallet.signTransaction(unsignedTransaction.getTransaction());
        final SignedTransaction signedTransaction = new SignedTransaction()
                .setEncodedTransaction(unsignedTransaction.getTransaction())
                .setSignature(signature);

        BalanceService.getApi()
                .sendSignedTransaction(timestamp, signedTransaction)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(new SingleSubscriber<SentTransaction>() {
                    @Override
                    public void onSuccess(final SentTransaction sentTransaction) {
                        callback.onSuccess(sentTransaction.getTxHash());
                    }

                    @Override
                    public void onError(final Throwable error) {
                        callback.onError(error);
                    }
                });
    }
}