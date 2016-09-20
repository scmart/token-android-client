package com.bakkenbaeck.toshi.network.ws;


import com.bakkenbaeck.toshi.network.rest.model.TransactionSent;
import com.bakkenbaeck.toshi.network.ws.model.ConnectionState;
import com.bakkenbaeck.toshi.network.ws.model.Payment;
import com.bakkenbaeck.toshi.network.ws.model.TransactionConfirmation;

import rx.Observable;
import rx.subjects.BehaviorSubject;

public class SocketObservables {

    private final BehaviorSubject<Payment> paymentSubject = BehaviorSubject.create();
    private final BehaviorSubject<TransactionConfirmation> transactionConfirmationSubject = BehaviorSubject.create();
    private final BehaviorSubject<TransactionSent> transactionSentSubject = BehaviorSubject.create();
    private final BehaviorSubject<ConnectionState> connectionObservable = BehaviorSubject.create(ConnectionState.CONNECTING);

    public Observable<Payment> getPaymentObservable() {
        return this.paymentSubject.asObservable();
    }

    public Observable<TransactionConfirmation> getTransactionConfirmationObservable() {
        return this.transactionConfirmationSubject.asObservable();
    }

    public Observable<TransactionSent> getTransactionSentObservable() {
        return this.transactionSentSubject.asObservable();
    }

    public Observable<ConnectionState> getConnectionObservable() {
        return this.connectionObservable.asObservable();
    }

    public void emitPayment(final Payment payment) {
        this.paymentSubject.onNext(payment);
    }

    public void emitTransactionConfirmation(final TransactionConfirmation transactionConfirmation) {
        this.transactionConfirmationSubject.onNext(transactionConfirmation);
    }

    public void emitTransactionSent(final TransactionSent transactionSent) {
        this.transactionSentSubject.onNext(transactionSent);
    }

    public void emitNewConnectionState(final ConnectionState newState) {
        this.connectionObservable.onNext(newState);
    }
}
