package com.bakkenbaeck.token.network.util;


import com.bakkenbaeck.token.crypto.HDWallet;
import com.bakkenbaeck.token.model.network.SentTransaction;
import com.bakkenbaeck.token.model.network.ServerTime;
import com.bakkenbaeck.token.model.network.SignedTransaction;
import com.bakkenbaeck.token.model.network.TransactionRequest;
import com.bakkenbaeck.token.model.network.UnsignedTransaction;
import com.bakkenbaeck.token.network.BalanceService;

import java.math.BigDecimal;

import rx.SingleSubscriber;
import rx.schedulers.Schedulers;

public class Transaction {

    private final BigDecimal ethAmount;
    private final String toAddress;
    private final HDWallet userWallet;
    private final SingleSubscriber<SentTransaction> callback;

    public Transaction(
            final BigDecimal ethAmount,
            final String toAddress,
            final HDWallet userWallet,
            final SingleSubscriber<SentTransaction> callback) {
        this.ethAmount = ethAmount;
        this.toAddress = toAddress;
        this.userWallet = userWallet;
        this.callback = callback;

    }

    // Responsible for creating, signing, and broadcasting an ethereum transaction
    // to the ethereum network.
    public final void process() {

        final TransactionRequest transactionRequest = new TransactionRequest()
                .setValue(this.ethAmount)
                .setFromAddress(this.userWallet.getWalletAddress())
                .setToAddress(this.toAddress);

        BalanceService.getApi().createTransaction(transactionRequest)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(new SingleSubscriber<UnsignedTransaction>() {
                    @Override
                    public void onSuccess(final UnsignedTransaction unsignedTransaction) {
                        fetchServerTimeForUnsignedTransaction(unsignedTransaction);
                    }

                    @Override
                    public void onError(final Throwable error) {
                        callback.onError(error);
                    }
                });
    }

    private void fetchServerTimeForUnsignedTransaction(final UnsignedTransaction unsignedTransaction) {
        BalanceService
                .getApi()
                .getTimestamp()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(new SingleSubscriber<ServerTime>() {
                    @Override
                    public void onSuccess(final ServerTime serverTime) {
                        signTransactionWithTimeStamp(unsignedTransaction, serverTime.get());
                    }

                    @Override
                    public void onError(final Throwable error) {
                        callback.onError(error);
                    }
                });
    }

    private void signTransactionWithTimeStamp(
            final UnsignedTransaction unsignedTransaction,
            final long timestamp) {

        final String signature = userWallet.signTransaction(unsignedTransaction.getTransaction());
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
                        callback.onSuccess(sentTransaction);
                    }

                    @Override
                    public void onError(final Throwable error) {
                        callback.onError(error);
                    }
                });
    }
}
