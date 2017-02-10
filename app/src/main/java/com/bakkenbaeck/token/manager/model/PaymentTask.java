package com.bakkenbaeck.token.manager.model;


import android.support.annotation.IntDef;

import com.bakkenbaeck.token.model.sofa.Payment;

public class PaymentTask {

    @IntDef({INCOMING,OUTGOING})
    public @interface Action {}
    public static final int INCOMING = 0;
    public static final int OUTGOING = 1;

    private final Payment payment;
    private final @Action int action;

    public PaymentTask(
            final Payment payment,
            final @Action int action) {
        this.payment = payment;
        this.action = action;
    }

    public Payment getPayment() {
        return this.payment;
    }

    public int getAction() {
        return this.action;
    }
}
