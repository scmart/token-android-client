package com.bakkenbaeck.token.model.sofa;


import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

import java.io.IOException;

public class SofaAdapters {

    private final Moshi moshi;
    private final JsonAdapter<Message> messageAdapter;
    private final JsonAdapter<PaymentRequest> paymentRequestAdapter;
    private final JsonAdapter<Command> commandAdapter;
    private final JsonAdapter<Payment> paymentAdapter;

    public SofaAdapters() {
        this.moshi = new Moshi.Builder().build();
        this.messageAdapter = moshi.adapter(Message.class);
        this.paymentRequestAdapter = moshi.adapter(PaymentRequest.class);
        this.commandAdapter = moshi.adapter(Command.class);
        this.paymentAdapter = moshi.adapter(Payment.class);
    }

    public String toJson(final Message sofaMessage) {
        return SofaType.createHeader(SofaType.PLAIN_TEXT) + this.messageAdapter.toJson(sofaMessage);
    }

    public String toJson(final PaymentRequest paymentRequest) {
        return SofaType.createHeader(SofaType.PAYMENT_REQUEST) + this.paymentRequestAdapter.toJson(paymentRequest);
    }

    public String toJson(final Command sofaCommand) {
        return SofaType.createHeader(SofaType.COMMAND_REQUEST) + this.commandAdapter.toJson(sofaCommand);
    }
    public String toJson(final Payment payment) {
        return SofaType.createHeader(SofaType.PAYMENT) + this.paymentAdapter.toJson(payment);
    }

    public Message messageFrom(final String payload) throws IOException {
        return messageAdapter.fromJson(payload);
    }

    public PaymentRequest txRequestFrom(final String payload) throws IOException {
        return paymentRequestAdapter.fromJson(payload);
    }

    public Payment paymentFrom(final String payload) throws IOException {
        return paymentAdapter.fromJson(payload);
    }
}
