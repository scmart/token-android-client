package com.bakkenbaeck.toshi.network.ws;


import com.bakkenbaeck.toshi.network.ws.WebSocketConnection.Listener;
import com.bakkenbaeck.toshi.network.ws.model.SocketToPojo;

public class WebSocketManager {

    private SocketObservables socketObservables;
    private WebSocketConnection webSocketConnection;
    private SocketToPojo socketToPojo;

    public WebSocketManager() {
        this.socketObservables = new SocketObservables();
        this.webSocketConnection = new WebSocketConnection(this.jsonMessageListener);
        this.socketToPojo = new SocketToPojo(this.socketObservables);
    }

    public void init(final String userId) {
        this.webSocketConnection.init(userId);
    }

    private final Listener jsonMessageListener = new Listener() {
        @Override
        public void onJsonMessage(final String json) {
            socketToPojo.handleNewMessage(json);
        }
    };

    public final SocketObservables getSocketObservables() {
        return this.socketObservables;
    }
}
