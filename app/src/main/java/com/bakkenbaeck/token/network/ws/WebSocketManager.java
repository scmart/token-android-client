package com.bakkenbaeck.token.network.ws;


import com.bakkenbaeck.token.network.ws.WebSocketConnection.Listener;
import com.bakkenbaeck.token.network.ws.model.ConnectionState;
import com.bakkenbaeck.token.network.ws.model.SocketToPojo;

public class WebSocketManager {
    public static final String AD_BOT_ID = "32a2299bd8dc405da979471275db2a5e";

    private SocketObservables socketObservables;
    private WebSocketConnection webSocketConnection;
    private SocketToPojo socketToPojo;

    public WebSocketManager() {
        this.socketObservables = new SocketObservables();
        this.webSocketConnection = new WebSocketConnection(this.jsonMessageListener);
        this.socketToPojo = new SocketToPojo(this.socketObservables);
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

    public void disconnect(){
        if(webSocketConnection != null){
            webSocketConnection.disconnect();
        }
    }

    public final SocketObservables getSocketObservables() {
        return this.socketObservables;
    }

    public final void sendMessage(final String message) {
        this.webSocketConnection.sendMessage(message);
    }
}
