package com.bakkenbaeck.token.model.sofa;


import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

import java.io.IOException;

public class SofaAdapters {

    private final Moshi moshi;
    private final JsonAdapter<Message> messageAdapter;
    private final JsonAdapter<TxRequest> paymentRequestAdapter;

    public SofaAdapters() {
        this.moshi = new Moshi.Builder().build();
        this.messageAdapter = moshi.adapter(Message.class);
        this.paymentRequestAdapter = moshi.adapter(TxRequest.class);
    }

    public String toJson(final Message sofaMessage) {
        return SofaType.createHeader(SofaType.PLAIN_TEXT) + this.messageAdapter.toJson(sofaMessage);
    }

    public String toJson(final TxRequest txRequest) {
        return SofaType.createHeader(SofaType.PAYMENT_REQUEST) + this.paymentRequestAdapter.toJson(txRequest);
    }

    public Message messageFrom(final String payload) throws IOException {
        return messageAdapter.fromJson(payload);
    }

    public TxRequest txRequestFrom(final String payload) throws IOException {
        return paymentRequestAdapter.fromJson(payload);
    }
}
