package com.bakkenbaeck.toshi.network.ws;


import com.bakkenbaeck.toshi.network.ws.model.Payment;

import rx.Observable;
import rx.subjects.PublishSubject;

public class SocketObservables {

    private final PublishSubject<Payment> paymentSubject = PublishSubject.create();

    public Observable<Payment> getPaymentObservable() {
        return this.paymentSubject.asObservable();
    }

    public void emit(final Payment payment) {
        this.paymentSubject.onNext(payment);
    }
}
