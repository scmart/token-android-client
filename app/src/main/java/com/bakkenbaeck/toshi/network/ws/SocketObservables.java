package com.bakkenbaeck.toshi.network.ws;


import com.bakkenbaeck.toshi.network.ws.model.Payment;

import rx.Observable;
import rx.subjects.BehaviorSubject;

public class SocketObservables {

    private final BehaviorSubject<Payment> paymentSubject = BehaviorSubject.create();

    public Observable<Payment> getPaymentObservable() {
        return this.paymentSubject.asObservable();
    }

    public void emit(final Payment payment) {
        this.paymentSubject.onNext(payment);
    }
}
