package com.bakkenbaeck.token.network.ws;


import android.os.Handler;

import com.bakkenbaeck.token.util.LogUtil;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFactory;
import com.neovisionaries.ws.client.WebSocketFrame;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/* package */ class WebSocketConnection {
    /* package */ interface Listener {
        void onJsonMessage(final String json);
        void onReconnecting();
        void onConnected();
    }

    private final WebSocketFactory wsFactory;
    private final Handler reconnectHandler;
    private final Listener listener;
    private boolean connected = false;

    private WebSocket webSocket;

    public WebSocketConnection(final Listener listener) {
        this.wsFactory = new WebSocketFactory();
        this.reconnectHandler = new Handler();
        this.listener = listener;
    }

    public void init(final String url) {
        if(!connected) {
            try {
                if(this.webSocket != null){
                    webSocket.disconnect();
                    webSocket.clearListeners();
                }
                this.webSocket = wsFactory.createSocket(url);
                this.webSocket.addListener(new WebSocketAdapter() {
                    @Override
                    public void onConnected(final WebSocket websocket, final Map<String, List<String>> headers) throws Exception {
                        LogUtil.i(getClass(), "Connected");
                        listener.onConnected();
                        connected = true;
                        websocket.setPingInterval(15 * 1000);
                    }

                    @Override
                    public void onConnectError(final WebSocket websocket, final WebSocketException cause) throws Exception {
                        LogUtil.e(getClass(), "Connected Error");
                        connected = false;
                        listener.onReconnecting();
                    }

                    @Override
                    public void onDisconnected(final WebSocket websocket, final WebSocketFrame serverCloseFrame, final WebSocketFrame clientCloseFrame, final boolean closedByServer) throws Exception {
                        LogUtil.e(getClass(), "Disconnected");
                        connected = false;
                        listener.onReconnecting();
                    }

                    @Override
                    public void onTextMessage(final WebSocket websocket, final String message) throws Exception {
                        listener.onJsonMessage(message);
                    }
                });
                this.webSocket.connectAsynchronously();
            } catch (final IOException e) {
                LogUtil.e(getClass(), "Connect failed. " + e);
                listener.onReconnecting();
            }
        }
    }

    public void disconnect(){
        LogUtil.e(getClass(), "disconnect");
        if(webSocket != null){
            connected = false;
            webSocket.disconnect();
            webSocket.clearListeners();
        }
    }

    private void reconnect() {
        LogUtil.e(getClass(), "Reconnect!");
        this.listener.onReconnecting();
        this.reconnectHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    webSocket = webSocket.recreate().connectAsynchronously();
                } catch (final IOException e) {
                    LogUtil.e(getClass(), "Reconnect failed. " + e);
                    reconnect();
                }
            }
        }, 10 * 1000);

    }

    public boolean isConnected(){
        return connected;
    }

    public void sendMessage(final String message) {
        this.webSocket.sendText(message);
    }
}
