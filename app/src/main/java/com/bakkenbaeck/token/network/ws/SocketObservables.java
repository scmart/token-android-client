package com.bakkenbaeck.token.network.ws;


import com.bakkenbaeck.token.network.rest.model.TransactionSent;
import com.bakkenbaeck.token.network.rest.model.VerificationSent;
import com.bakkenbaeck.token.network.ws.model.ConnectionState;
import com.bakkenbaeck.token.network.ws.model.Message;
import com.bakkenbaeck.token.network.ws.model.TransactionConfirmation;
import com.bakkenbaeck.token.network.ws.model.VerificationSuccess;
import com.bakkenbaeck.token.network.ws.model.WebSocketError;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

public class SocketObservables {

    private final BehaviorSubject<TransactionConfirmation> transactionConfirmationSubject = BehaviorSubject.create();
    private final PublishSubject<Message> messageSubject = PublishSubject.create();
    private final PublishSubject<VerificationSent> verificationSentSubject = PublishSubject.create();
    private final PublishSubject<VerificationSuccess> verificationSuccessSubject = PublishSubject.create();
    private final BehaviorSubject<TransactionSent> transactionSentSubject = BehaviorSubject.create();
    private final PublishSubject<WebSocketError> errorSubject = PublishSubject.create();
    private final BehaviorSubject<ConnectionState> connectionObservable = BehaviorSubject.create(ConnectionState.CONNECTING);

    public Observable<TransactionConfirmation> getTransactionConfirmationObservable() {
        return this.transactionConfirmationSubject.asObservable();
    }

    public Observable<TransactionSent> getTransactionSentObservable() {
        return this.transactionSentSubject.asObservable();
    }

    public Observable<ConnectionState> getConnectionObservable() {
        return this.connectionObservable.asObservable();
    }

    public Observable<WebSocketError> getErrorObservable() {
        return this.errorSubject.asObservable();
    }

    public Observable<Message> getMessageObservable() {
        return this.messageSubject.asObservable();
    }

    public Observable<VerificationSent> getVerificationSentObservable() {
        return this.verificationSentSubject.asObservable();
    }

    public Observable<VerificationSuccess> getVerificationSuccessObservable() {
        return this.verificationSuccessSubject.asObservable().observeOn(AndroidSchedulers.mainThread());
    }

    public void emitTransactionConfirmation(final TransactionConfirmation transactionConfirmation) {
        this.transactionConfirmationSubject.onNext(transactionConfirmation);
    }

    public void emitVerificationSent(final VerificationSent verificationSent) {
        this.verificationSentSubject.onNext(verificationSent);
    }

    public void emitVerificationSuccess(final VerificationSuccess verificationSuccess) {
        this.verificationSuccessSubject.onNext(verificationSuccess);
    }

    public void emitError(final WebSocketError error) {
        this.errorSubject.onNext(error);
    }

    public void emitMessage(final Message message) {
        this.messageSubject.onNext(message);
    }

    public void emitTransactionSent(final TransactionSent transactionSent) {
        this.transactionSentSubject.onNext(transactionSent);
    }

    public void emitNewConnectionState(final ConnectionState newState) {
        this.connectionObservable.onNext(newState);
    }
}
