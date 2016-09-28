package com.bakkenbaeck.toshi.network.ws.model;


import com.bakkenbaeck.toshi.model.jsonadapter.BigIntegerAdapter;
import com.bakkenbaeck.toshi.network.rest.model.TransactionSent;
import com.bakkenbaeck.toshi.network.ws.SocketObservables;
import com.bakkenbaeck.toshi.util.LogUtil;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

import java.io.IOException;

public class SocketToPojo {

    private final Moshi moshi;
    private final JsonAdapter<WebSocketMessage> jsonAdapter;
    private final JsonAdapter<TransactionSent> transactionSentAdapter;
    private final JsonAdapter<TransactionConfirmation> confirmationAdapter;
    private final JsonAdapter<WebSocketError> errorAdapter;
    private final SocketObservables socketObservables;

    public SocketToPojo(final SocketObservables socketObservables) {
        this.socketObservables = socketObservables;
        this.moshi = new Moshi
                            .Builder()
                            .add(new BigIntegerAdapter())
                            .build();
        this.jsonAdapter = this.moshi.adapter(WebSocketMessage.class);
        this.transactionSentAdapter = this.moshi.adapter(TransactionSent.class);
        this.confirmationAdapter = this.moshi.adapter(TransactionConfirmation.class);
        this.errorAdapter = this.moshi.adapter(WebSocketError.class);
    }

    public void handleNewMessage(final String json) {
        try {
            convertAndEmitPojo(json);
        } catch (final IOException e) {
            LogUtil.e(getClass(), e.toString());
        }

    }

    private void convertAndEmitPojo(final String json) throws IOException {
        LogUtil.i(getClass(), "Incoming WS event. " + json);
        final WebSocketMessage message = getWebSocketMessageFromJson(json);
        if (message == null) {
            LogUtil.e(getClass(), "Websocket frame unhandled");
            return;
        }

        switch (message.type) {
            case "hello":
                LogUtil.i(getClass(), "Ignoring websocket event -- hello");
                break;
            case "transaction_sent":
                LogUtil.i(getClass(), "Handling websocket event -- transaction_sent");
                final TransactionSent transactionSent = this.transactionSentAdapter.fromJson(json);
                this.socketObservables.emitTransactionSent(transactionSent);
                break;
            case "transaction_confirmation":
                LogUtil.i(getClass(), "Handling websocket event -- transaction_confirmation");
                final TransactionConfirmation confirmation = this.confirmationAdapter.fromJson(json);
                this.socketObservables.emitTransactionConfirmation(confirmation);
                break;
            case "error":
                LogUtil.i(getClass(), "Handling websocket event -- error");
                final WebSocketError error = this.errorAdapter.fromJson(json);
                this.socketObservables.emitError(error);
                break;
            default:
                LogUtil.e(getClass(), "Unrecognised websocket event - " + message.type);
                LogUtil.e(getClass(), "Full event - " + json);
        }
    }

    private WebSocketMessage getWebSocketMessageFromJson(final String message) {
        try {
            final WebSocketMessage webSocketMessage = this.jsonAdapter.fromJson(message);
            return webSocketMessage;
        } catch (final IOException e) {
            LogUtil.e(getClass(), "Invalid JSON input. " + e);
            return null;
        }
    }
}
