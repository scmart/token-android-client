package com.bakkenbaeck.token.network.ws;


import com.bakkenbaeck.token.network.ws.model.ConnectionState;
import com.bakkenbaeck.token.network.ws.model.Message;
import com.bakkenbaeck.token.network.ws.model.WebSocketError;

import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

public class SocketObservables {
    private final PublishSubject<Message> messageSubject = PublishSubject.create();
    private final PublishSubject<WebSocketError> errorSubject = PublishSubject.create();
    private final BehaviorSubject<ConnectionState> connectionObservable = BehaviorSubject.create(ConnectionState.CONNECTING);


    public void emitError(final WebSocketError error) {
        this.errorSubject.onNext(error);
    }

    public void emitMessage(final Message message) {
        this.messageSubject.onNext(message);
    }

    public void emitNewConnectionState(final ConnectionState newState) {
        this.connectionObservable.onNext(newState);
    }
}
