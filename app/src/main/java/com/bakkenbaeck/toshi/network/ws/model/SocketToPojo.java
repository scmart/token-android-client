package com.bakkenbaeck.toshi.network.ws.model;


import com.bakkenbaeck.toshi.network.ws.SocketObservables;
import com.bakkenbaeck.toshi.util.LogUtil;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

import java.io.IOException;

public class SocketToPojo {

    private final Moshi moshi;
    private final JsonAdapter<WebSocketMessage> jsonAdapter;
    private final JsonAdapter<Payment> paymentAdapter;
    private final JsonAdapter<TransactionConfirmation> confirmationAdapter;
    private final SocketObservables socketObservables;

    public SocketToPojo(final SocketObservables socketObservables) {
        this.socketObservables = socketObservables;
        this.moshi = new Moshi
                            .Builder()
                            .build();
        this.jsonAdapter = this.moshi.adapter(WebSocketMessage.class);
        this.paymentAdapter = this.moshi.adapter(Payment.class);
        this.confirmationAdapter = this.moshi.adapter(TransactionConfirmation.class);
    }

    public void handleNewMessage(final String json) {
        try {
            convertAndEmitPojo(json);
        } catch (final IOException e) {
            LogUtil.e(getClass(), e.toString());
        }

    }

    private void convertAndEmitPojo(final String json) throws IOException {
        final WebSocketMessage message = getWebSocketMessageFromJson(json);
        if (message == null) {
            return;
        }

        switch (message.type) {
            case "hello":
                LogUtil.i(getClass(), "Ignoring websocket event -- hello");
                break;
            case "payment":
                LogUtil.i(getClass(), "Handling websocket event -- payment");
                final Payment payment = this.paymentAdapter.fromJson(json);
                this.socketObservables.emitPayment(payment);
                break;
            case "transaction_confirmation":
                LogUtil.i(getClass(), "Handling websocket event -- transaction_confirmation");
                final TransactionConfirmation confirmation = this.confirmationAdapter.fromJson(json);
                this.socketObservables.emitTransactionConfirmation(confirmation);
                break;
            default:
                LogUtil.e(getClass(), "Unrecognised websocket event - " + message.type);
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
