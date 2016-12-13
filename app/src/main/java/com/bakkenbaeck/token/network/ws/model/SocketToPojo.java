package com.bakkenbaeck.token.network.ws.model;


import com.bakkenbaeck.token.manager.WebSocketManager;
import com.bakkenbaeck.token.model.jsonadapter.BigIntegerAdapter;
import com.bakkenbaeck.token.network.rest.model.TransactionSent;
import com.bakkenbaeck.token.network.rest.model.VerificationSent;
import com.bakkenbaeck.token.network.ws.SocketObservables;
import com.bakkenbaeck.token.util.LogUtil;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

import java.io.IOException;

public class SocketToPojo {
    private static final String TOKEN_ID = "token";

    private final Moshi moshi;
    private final JsonAdapter<WebSocketType> jsonAdapter;
    private final JsonAdapter<Message> messageAdapter;
    private final JsonAdapter<TransactionSent> transactionSentAdapter;
    private final JsonAdapter<TransactionConfirmation> confirmationAdapter;
    private final JsonAdapter<VerificationSent> verificationSentAdapter;
    private final JsonAdapter<VerificationSuccess> verificationSuccessAdapter;
    private final JsonAdapter<WebSocketError> errorAdapter;
    private final SocketObservables socketObservables;

    public SocketToPojo(final SocketObservables socketObservables) {
        this.socketObservables = socketObservables;
        this.moshi = new Moshi
                            .Builder()
                            .add(new BigIntegerAdapter())
                            .build();
        this.jsonAdapter = this.moshi.adapter(WebSocketType.class);
        this.messageAdapter = this.moshi.adapter(Message.class);
        this.transactionSentAdapter = this.moshi.adapter(TransactionSent.class);
        this.confirmationAdapter = this.moshi.adapter(TransactionConfirmation.class);
        this.verificationSentAdapter = this.moshi.adapter(VerificationSent.class);
        this.verificationSuccessAdapter = this.moshi.adapter(VerificationSuccess.class);
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
        final WebSocketType webSocketType = getWebSocketMessageFromJson(json);
        if (webSocketType == null) {
            LogUtil.e(getClass(), "Websocket frame unhandled");
            return;
        }

        switch (webSocketType.get()) {
            case "hello":
                LogUtil.i(getClass(), "Ignoring websocket event -- hello");
                break;
            case "message":
                if (webSocketType.getSenderId().equals(WebSocketManager.AD_BOT_ID) || webSocketType.getSenderId().equals(TOKEN_ID)) {
                    final Message message = this.messageAdapter.fromJson(json);
                    this.socketObservables.emitMessage(message);
                    break;
                } else {
                    LogUtil.i(getClass(), "convertAndEmitPojo: UNKNOWN SENDER ID");
                    break;
                }
            case "transaction_sent":
                final TransactionSent transactionSent = this.transactionSentAdapter.fromJson(json);
                this.socketObservables.emitTransactionSent(transactionSent);
                break;
            case "transaction_confirmation":
                final TransactionConfirmation confirmation = this.confirmationAdapter.fromJson(json);
                this.socketObservables.emitTransactionConfirmation(confirmation);
                break;
            case "verification_sent":
                LogUtil.i(getClass(), "convertAndEmitPojo: verification_sent");
                final VerificationSent verificationSent = this.verificationSentAdapter.fromJson(json);
                this.socketObservables.emitVerificationSent(verificationSent);
                break;
            case "verification_success":
                LogUtil.i(getClass(),  "convertAndEmitPojo: verification_success");
                final VerificationSuccess verificationMessage = this.verificationSuccessAdapter.fromJson(json);
                this.socketObservables.emitVerificationSuccess(verificationMessage);
                
                break;
                case "message_sent":
                //Do nothing
                break;
            case "payment_request_sent":
                //Do nothing
                break;
            case "error":
                WebSocketError error;
                try {
                    error = this.errorAdapter.fromJson(json);
                } catch (final Exception ex) {
                    LogUtil.e(getClass(), "Unrecognised error code. Emitting generic error.");
                    error = new WebSocketError();
                }
                this.socketObservables.emitError(error);
                break;
            default:
                LogUtil.e(getClass(), "Unrecognised websocket event - " + webSocketType.get());
        }
    }

    private WebSocketType getWebSocketMessageFromJson(final String message) {
        try {
            return this.jsonAdapter.fromJson(message);
        } catch (final IOException e) {
            LogUtil.e(getClass(), "Invalid JSON input. " + e);
            return null;
        }
    }
}
