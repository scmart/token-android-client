package com.tokenbrowser.manager.model;


import android.support.annotation.IntDef;

import com.tokenbrowser.model.local.User;
import com.tokenbrowser.model.sofa.Payment;

public class PaymentTask {


    @IntDef({INCOMING, OUTGOING})
    public @interface Action {}
    public static final int INCOMING = 0;
    public static final int OUTGOING = 1;

    private final User user;
    private final Payment payment;
    private final @Action int action;

    public PaymentTask(
            final User user,
            final Payment payment,
            final @Action int action) {
        this.user = user;
        this.payment = payment;
        this.action = action;
    }

    public User getUser() {
        return this.user;
    }

    public Payment getPayment() {
        return this.payment;
    }

    public int getAction() {
        return this.action;
    }
}
