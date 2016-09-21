package com.bakkenbaeck.toshi.network.ws;


import com.bakkenbaeck.toshi.model.User;
import com.bakkenbaeck.toshi.network.rest.ToshiService;
import com.bakkenbaeck.toshi.network.rest.model.WebSocketConnectionDetails;
import com.bakkenbaeck.toshi.network.ws.WebSocketConnection.Listener;
import com.bakkenbaeck.toshi.network.ws.model.ConnectionState;
import com.bakkenbaeck.toshi.network.ws.model.SocketToPojo;
import com.bakkenbaeck.toshi.util.RetryWithBackoff;
import com.bakkenbaeck.toshi.view.BaseApplication;

import rx.Subscriber;

public class WebSocketManager {

    private SocketObservables socketObservables;
    private WebSocketConnection webSocketConnection;
    private SocketToPojo socketToPojo;

    public WebSocketManager() {
        this.socketObservables = new SocketObservables();
        this.webSocketConnection = new WebSocketConnection(this.jsonMessageListener);
        this.socketToPojo = new SocketToPojo(this.socketObservables);

        BaseApplication.get().getUserManager().getObservable().subscribe(this.newUserSubscriber);
    }

    private void init(final String url) {
        this.webSocketConnection.init(url);
    }

    private final Listener jsonMessageListener = new Listener() {
        @Override
        public void onJsonMessage(final String json) {
            socketToPojo.handleNewMessage(json);
        }

        @Override
        public void onReconnecting() {
            socketObservables.emitNewConnectionState(ConnectionState.CONNECTING);
        }

        @Override
        public void onConnected() {
            socketObservables.emitNewConnectionState(ConnectionState.CONNECTED);
        }
    };

    private final Subscriber<User> newUserSubscriber = new Subscriber<User>() {
        @Override
        public void onCompleted() {}

        @Override
        public void onError(final Throwable e) {}

        @Override
        public void onNext(final User user) {
            this.unsubscribe();
            ToshiService.getApi()
                    .getWebsocketUrl(user.getAuthToken())
                    .retryWhen(new RetryWithBackoff(10))
                    .subscribe(this.webConnectionDetailsSubscriber);
        }

        private final Subscriber<WebSocketConnectionDetails> webConnectionDetailsSubscriber = new Subscriber<WebSocketConnectionDetails>() {
            @Override
            public void onCompleted() {}

            @Override
            public void onError(final Throwable e) {}

            @Override
            public void onNext(final WebSocketConnectionDetails webSocketConnectionDetails) {
                this.unsubscribe();
                init(webSocketConnectionDetails.getUrl());
            }
        };
    };

    public final SocketObservables getSocketObservables() {
        return this.socketObservables;
    }
}
