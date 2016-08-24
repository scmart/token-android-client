package com.bakkenbaeck.toshi.manager;


import android.os.Handler;

import com.bakkenbaeck.toshi.util.LogUtil;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFactory;
import com.neovisionaries.ws.client.WebSocketFrame;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class WebsocketManager {

    private final WebSocketFactory wsFactory;
    private final Handler reconnectHandler;

    private WebSocket webSocket;


    public WebsocketManager() {
        this.wsFactory = new WebSocketFactory();
        this.reconnectHandler = new Handler();
    }

    public void init(final String userId) {
        try {
            this.webSocket = wsFactory.createSocket("ws://toshi-app.herokuapp.com/ws/" + userId);
            this.webSocket.addListener(new WebSocketAdapter() {
                @Override
                public void onConnected(final WebSocket websocket, final Map<String, List<String>> headers) throws Exception {
                    LogUtil.i(getClass(), "Connected");
                    websocket.setPingInterval(50 * 1000);
                }

                @Override
                public void onConnectError(final WebSocket websocket, final WebSocketException cause) throws Exception {
                    LogUtil.e(getClass(), "Connected Error");
                    reconnect();
                }

                @Override
                public void onDisconnected(final WebSocket websocket, final WebSocketFrame serverCloseFrame, final WebSocketFrame clientCloseFrame, final boolean closedByServer) throws Exception {
                    LogUtil.e(getClass(), "Disconnected");
                    reconnect();
                }

                @Override
                public void onTextMessage(final WebSocket websocket, final String text) throws Exception {
                    LogUtil.w(getClass(), text);
                }
            });
            this.webSocket.connectAsynchronously();
        } catch (final IOException e) {
            LogUtil.e(getClass(), "Connect failed. " + e);
            reconnect();
        }
    }

    private void reconnect() {
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
}
