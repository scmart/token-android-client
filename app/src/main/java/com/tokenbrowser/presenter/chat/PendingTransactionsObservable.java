package com.tokenbrowser.presenter.chat;


import com.tokenbrowser.model.local.PendingTransaction;
import com.tokenbrowser.model.local.SofaMessage;
import com.tokenbrowser.model.local.User;
import com.tokenbrowser.model.sofa.Payment;
import com.tokenbrowser.model.sofa.SofaAdapters;
import com.tokenbrowser.view.BaseApplication;

import java.io.IOException;

import rx.Observable;

/**
 * An Observable that only returns PendingTransactions for a single user.
 * Example usage:
 * <pre> {@code

PendingTransactionsObservable pto = new PendingTransactionsObservable();
Observable<PendingTransaction> observable = pto.init(user); // Only PendingTransactions involving this user will be returned
} </pre>
 */
/* package */ class PendingTransactionsObservable {

    private User remoteUser;
    private SofaAdapters adapters;

    /**
     * Initialises PendingTransactionsObservable with the user whose PendingTransactions to observe
     * <p>
     * @param remoteUser
     *              The user to be observed.
     */
    /* package */ Observable<PendingTransaction> init(final User remoteUser) {
        this.remoteUser = remoteUser;
        this.adapters = new SofaAdapters();
        return subscribeToPendingTransactionChanges();
    }

    private Observable<PendingTransaction> subscribeToPendingTransactionChanges() {
        return
                BaseApplication
                .get()
                .getTokenManager()
                .getTransactionManager()
                .getPendingTransactionObservable()
                .filter(this::shouldBeBroadcast);
    }

    private boolean shouldBeBroadcast(final PendingTransaction pendingTransaction) {
        try {
            final SofaMessage sofaMessage = pendingTransaction.getSofaMessage();
            final Payment payment = this.adapters.paymentFrom(sofaMessage.getPayload());
            final @Payment.PaymentDirection int paymentDirection =
                    payment.getPaymentDirection()
                            .toBlocking()
                            .value();
            return paymentDirection != Payment.NOT_RELEVANT
                    && isWatchingRemoteAddress(payment, paymentDirection);
        } catch (final IOException ex) {
            return false;
        }
    }

    private boolean isWatchingRemoteAddress(final Payment payment, final @Payment.PaymentDirection int paymentDirection) {
        final String remoteAddress = paymentDirection == Payment.FROM_LOCAL_USER
                ? payment.getToAddress()
                : payment.getFromAddress();
        return remoteAddress.equals(this.remoteUser.getPaymentAddress());
    }
}
