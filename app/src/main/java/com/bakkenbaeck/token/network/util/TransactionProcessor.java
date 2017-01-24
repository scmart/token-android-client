package com.bakkenbaeck.token.network.util;


import com.bakkenbaeck.token.crypto.HDWallet;
import com.bakkenbaeck.token.model.network.SentTransaction;
import com.bakkenbaeck.token.model.network.SignedTransaction;
import com.bakkenbaeck.token.model.network.TransactionRequest;
import com.bakkenbaeck.token.model.network.UnsignedTransaction;
import com.bakkenbaeck.token.network.BalanceService;

import java.math.BigDecimal;

import rx.SingleSubscriber;
import rx.schedulers.Schedulers;

public class TransactionProcessor {

    // Responsible for creating, signing, and broadcasting an ethereum transaction
    // to the ethereum network.
    public final void process(
            final BigDecimal ethAmount,
            final String toAddress,
            final HDWallet userWallet,
            final SingleSubscriber<SentTransaction> callback) {

        final TransactionRequest transactionRequest = new TransactionRequest()
                .setValue(ethAmount)
                .setFromAddress(userWallet.getWalletAddress())
                .setToAddress(toAddress);

        BalanceService.getApi().createTransaction(transactionRequest)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(new SingleSubscriber<UnsignedTransaction>() {
                    @Override
                    public void onSuccess(final UnsignedTransaction unsignedTransaction) {
                        final String signature = userWallet.signTransaction(unsignedTransaction.getTransaction());
                        final SignedTransaction signedTransaction = new SignedTransaction()
                                .setEncodedTransaction(unsignedTransaction.getTransaction())
                                .setSignature(signature);

                        BalanceService.getApi()
                                .sendSignedTransaction(signedTransaction)
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

                    @Override
                    public void onError(final Throwable error) {
                        callback.onError(error);
                    }
                });
    }
}
