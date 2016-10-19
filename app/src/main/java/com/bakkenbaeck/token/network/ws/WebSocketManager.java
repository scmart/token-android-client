package com.bakkenbaeck.token.network.ws;


import android.os.Handler;
import android.util.Log;

import com.bakkenbaeck.token.model.User;
import com.bakkenbaeck.token.network.rest.TokenService;
import com.bakkenbaeck.token.network.rest.model.WebSocketConnectionDetails;
import com.bakkenbaeck.token.network.ws.WebSocketConnection.Listener;
import com.bakkenbaeck.token.network.ws.model.ConnectionState;
import com.bakkenbaeck.token.network.ws.model.SocketToPojo;
import com.bakkenbaeck.token.util.OnNextSubscriber;
import com.bakkenbaeck.token.util.RetryWithBackoff;
import com.bakkenbaeck.token.view.BaseApplication;

import rx.Subscriber;

public class WebSocketManager {
    private static final String TAG = "WebSocketManager";
    private SocketObservables socketObservables;
    private WebSocketConnection webSocketConnection;
    private SocketToPojo socketToPojo;

    public WebSocketManager() {
        this.socketObservables = new SocketObservables();
        this.webSocketConnection = new WebSocketConnection(this.jsonMessageListener);
        this.socketToPojo = new SocketToPojo(this.socketObservables);
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

            tryToReconnect();
        }

        @Override
        public void onConnected() {
            socketObservables.emitNewConnectionState(ConnectionState.CONNECTED);
        }
    };
    
    private void tryToReconnect(){
        if(!webSocketConnection.isConnected()){
            requestWebsocketConnection();
            Handler h = new Handler();
            h.postDelayed(new Runnable() {
                @Override
                public void run() {
                    tryToReconnect();
                }
            }, 1000 * 10);
        }
    }

    private final OnNextSubscriber<User> newUserSubscriber = new OnNextSubscriber<User>() {
        @Override
        public void onNext(final User user) {
            this.unsubscribe();
            TokenService.getApi()
                    .getWebsocketUrl(user.getAuthToken())
                    .retryWhen(new RetryWithBackoff(10))
                    .subscribe(webConnectionDetailsSubscriber);
        }
    };

    private final OnNextSubscriber<WebSocketConnectionDetails> webConnectionDetailsSubscriber = new OnNextSubscriber<WebSocketConnectionDetails>() {
        @Override
        public void onNext(final WebSocketConnectionDetails webSocketConnectionDetails) {
            this.unsubscribe();
            init(webSocketConnectionDetails.getUrl());
        }
    };

    public void requestWebsocketConnection(){
        if(!webSocketConnection.isConnected()) {
            BaseApplication.get().getUserManager().getObservable().subscribe(new OnNextSubscriber<User>() {
                @Override
                public void onNext(User user) {
                    if (user != null) {
                        TokenService.getApi()
                                .getWebsocketUrl(user.getAuthToken())
                                .retryWhen(new RetryWithBackoff(10))
                                .subscribe(new Subscriber<WebSocketConnectionDetails>() {
                                    @Override
                                    public void onCompleted() {
                                        Log.d(TAG, "onCompleted: ");
                                    }

                                    @Override
                                    public void onError(Throwable e) {
                                        Log.d(TAG, "onError: " + e);
                                    }

                                    @Override
                                    public void onNext(WebSocketConnectionDetails webSocketConnectionDetails) {
                                        Log.d(TAG, "onNext: ");
                                        init(webSocketConnectionDetails.getUrl());
                                    }
                                });
                    }
                }
            });
        }
    }

    public final SocketObservables getSocketObservables() {
        return this.socketObservables;
    }

    public final void sendMessage(final String message) {
        this.webSocketConnection.sendMessage(message);
    }
}
