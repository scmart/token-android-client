/*
 * 	Copyright (c) 2017. Token Browser, Inc
 *
 * 	This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.tokenbrowser.model.sofa;


import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

import java.io.IOException;

public class SofaAdapters {

    private final Moshi moshi;
    private final JsonAdapter<Message> messageAdapter;
    private final JsonAdapter<PaymentRequest> paymentRequestAdapter;
    private final JsonAdapter<Command> commandAdapter;
    private final JsonAdapter<Payment> paymentAdapter;
    private final JsonAdapter<Init> initAdapter;
    private final JsonAdapter<InitRequest> initRequestJsonAdapter;

    public SofaAdapters() {
        this.moshi = new Moshi.Builder().build();
        this.messageAdapter = moshi.adapter(Message.class);
        this.paymentRequestAdapter = moshi.adapter(PaymentRequest.class);
        this.commandAdapter = moshi.adapter(Command.class);
        this.paymentAdapter = moshi.adapter(Payment.class);
        this.initAdapter = moshi.adapter(Init.class);
        this.initRequestJsonAdapter = moshi.adapter(InitRequest.class);
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

    public String toJson(final Init init) {
        return SofaType.createHeader(SofaType.INIT) + this.initAdapter.toJson(init);
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

    public InitRequest initRequestFrom(final String payload) throws IOException {
        return initRequestJsonAdapter.fromJson(payload);
    }
}
