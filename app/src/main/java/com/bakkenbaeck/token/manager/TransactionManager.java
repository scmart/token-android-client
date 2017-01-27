package com.bakkenbaeck.token.manager;


import com.bakkenbaeck.token.crypto.HDWallet;
import com.bakkenbaeck.token.model.local.ChatMessage;
import com.bakkenbaeck.token.model.local.SendState;
import com.bakkenbaeck.token.model.local.Transaction;
import com.bakkenbaeck.token.model.network.SentTransaction;
import com.bakkenbaeck.token.model.network.ServerTime;
import com.bakkenbaeck.token.model.network.SignedTransaction;
import com.bakkenbaeck.token.model.network.TransactionRequest;
import com.bakkenbaeck.token.model.network.UnsignedTransaction;
import com.bakkenbaeck.token.model.sofa.Payment;
import com.bakkenbaeck.token.model.sofa.SofaAdapters;
import com.bakkenbaeck.token.network.BalanceService;
import com.bakkenbaeck.token.presenter.store.ChatMessageStore;
import com.bakkenbaeck.token.util.OnNextSubscriber;
import com.bakkenbaeck.token.view.BaseApplication;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import rx.SingleSubscriber;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

public class TransactionManager {

    private final PublishSubject<Transaction> transactionQueue = PublishSubject.create();

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

    public final void sendTransaction(final Transaction transaction) {
        this.transactionQueue.onNext(transaction);
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
        this.transactionQueue
                .observeOn(Schedulers.io())
                .subscribeOn(Schedulers.io())
                .subscribe(new OnNextSubscriber<Transaction>() {
                    @Override
                    public void onNext(final Transaction transaction) {
                        final ChatMessage chatMessage = generateMessageForTransaction(transaction);
                        storeMessage(chatMessage);
                        processTransaction(transaction, chatMessage);
                    }
                });
    }

    private ChatMessage generateMessageForTransaction(final Transaction transaction) {
        final Payment payment = new Payment(transaction);
        final String messageBody = this.adapters.toJson(payment);
        return new ChatMessage().makeNew(transaction.getOwnerAddress(), true, messageBody);
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

    private final void processTransaction(
            final Transaction transaction,
            final ChatMessage chatMessage) {

        final TransactionRequest transactionRequest = new TransactionRequest()
                .setValue(transaction.getEthAmount())
                .setFromAddress(this.wallet.getWalletAddress())
                .setToAddress(transaction.getToAddress());

        BalanceService.getApi().createTransaction(transactionRequest)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(new SingleSubscriber<UnsignedTransaction>() {
                    @Override
                    public void onSuccess(final UnsignedTransaction unsignedTransaction) {
                        fetchServerTimeForUnsignedTransaction(unsignedTransaction, chatMessage);
                    }

                    @Override
                    public void onError(final Throwable error) {
                        updateMessageToFailed(chatMessage);
                    }
                });
    }

    private void fetchServerTimeForUnsignedTransaction(
            final UnsignedTransaction unsignedTransaction,
            final ChatMessage chatMessage) {

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
                                serverTime.get());
                    }

                    @Override
                    public void onError(final Throwable error) {
                        updateMessageToFailed(chatMessage);
                    }
                });
    }

    private void signTransactionWithTimeStamp(
            final UnsignedTransaction unsignedTransaction,
            final ChatMessage chatMessage,
            final long timestamp) {

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
                        final String newPayload = embedTxHashInPayload(sentTransaction.getTxHash(), chatMessage.getPayload());
                        chatMessage.setPayload(newPayload);
                        updateMessageToSent(chatMessage);
                    }

                    private String embedTxHashInPayload(final String txHash, final String payload) {
                        try {
                            final Payment payment = adapters.paymentFrom(payload);
                            payment.setTxHash(txHash);
                            return adapters.toJson(payment);
                        } catch (IOException e) {
                            return payload;
                        }
                    }

                    @Override
                    public void onError(final Throwable error) {
                        updateMessageToFailed(chatMessage);
                    }
                });
    }
}