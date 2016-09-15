package com.bakkenbaeck.toshi.network.ws;


import com.bakkenbaeck.toshi.network.ws.model.Payment;
import com.bakkenbaeck.toshi.network.ws.model.TransactionConfirmation;

import rx.Observable;
import rx.subjects.BehaviorSubject;

public class SocketObservables {

    private final BehaviorSubject<Payment> paymentSubject = BehaviorSubject.create();
    private final BehaviorSubject<TransactionConfirmation> transactionConfirmationSubject = BehaviorSubject.create();

    public Observable<Payment> getPaymentObservable() {
        return this.paymentSubject.asObservable();
    }

    public Observable<TransactionConfirmation> getTransactionConfirmationObservable() {
        return this.transactionConfirmationSubject.asObservable();
    }

    public void emitPayment(final Payment payment) {
        this.paymentSubject.onNext(payment);
    }

    public void emitTransactionConfirmation(final TransactionConfirmation transactionConfirmation) {
        this.transactionConfirmationSubject.onNext(transactionConfirmation);
    }
}
