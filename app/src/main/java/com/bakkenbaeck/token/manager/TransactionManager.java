package com.bakkenbaeck.token.manager;


import com.bakkenbaeck.token.crypto.HDWallet;
import com.bakkenbaeck.token.model.local.ChatMessage;
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
import com.bakkenbaeck.token.util.LogUtil;
import com.bakkenbaeck.token.util.OnNextSubscriber;
import com.bakkenbaeck.token.view.BaseApplication;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import rx.SingleSubscriber;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

public class TransactionManager {

    private final PublishSubject<Payment> paymentQueue = PublishSubject.create();

    private HDWallet wallet;
    private ChatMessageStore chatMessageStore;
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
        this.paymentQueue.onNext(payment);
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
            }
        });
    }

    private void attachSubscribers() {
        this.paymentQueue
                .observeOn(Schedulers.io())
                .subscribeOn(Schedulers.io())
                .subscribe(new OnNextSubscriber<Payment>() {
                    @Override
                    public void onNext(final Payment transaction) {
                        final ChatMessage chatMessage = generateMessageFromPayment(transaction);
                        storeMessage(chatMessage);
                        final TransactionRequest transactionRequest = createTransactionRequest(transaction);
                        processTransactionRequest(
                                transactionRequest,
                                chatMessage,
                                new SingleSubscriber<String>() {
                                    @Override
                                    public void onSuccess(final String txHash) {
                                        transaction.setTxHash(txHash);
                                        final ChatMessage updatedMessage = generateMessageFromPayment(transaction);
                                        chatMessage.setPayload(updatedMessage.getPayload());
                                        updateMessageToSent(chatMessage);
                                    }

                                    @Override
                                    public void onError(final Throwable error) {
                                        LogUtil.e(getClass(), "Error creating transaction: " + error);
                                        updateMessageToFailed(chatMessage);
                                    }
                                }
                        );
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

    private void updateMessageToFailed(final ChatMessage message) {
        dbThreadExecutor.execute(new Runnable() {
            @Override
            public void run() {
                message.setSendState(SendState.STATE_FAILED);
                chatMessageStore.update(message);
            }
        });
    }

    private void updateMessageToSent(final ChatMessage message) {
        dbThreadExecutor.execute(new Runnable() {
            @Override
            public void run() {
                message.setSendState(SendState.STATE_SENT);
                chatMessageStore.update(message);

                BaseApplication
                        .get()
                        .getTokenManager()
                        .getChatMessageManager()
                        .sendMessage(message);
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
                        addTxHashToChatMessage(sentTransaction.getTxHash());
                        updateMessageToSent(chatMessage);
                    }

                    private void addTxHashToChatMessage(final String txHash) {
                        callback.onSuccess(txHash);
                    }

                    @Override
                    public void onError(final Throwable error) {
                        callback.onError(error);
                    }
                });
    }
}