package com.tokenbrowser.manager.model;


import android.support.annotation.IntDef;

import com.tokenbrowser.model.local.SofaMessage;
import com.tokenbrowser.model.local.User;

public class SofaMessageTask {

    @IntDef({SEND_AND_SAVE, SAVE_ONLY, SEND_ONLY})
    public @interface Action {}
    public static final int SEND_AND_SAVE = 0;
    public static final int SAVE_ONLY = 1;
    public static final int SEND_ONLY = 2;

    private final User receiver;
    private final SofaMessage sofaMessage;
    private final @Action int action;

    public SofaMessageTask(
            final User receiver,
            final SofaMessage sofaMessage,
            final @Action int action) {
        this.receiver = receiver;
        this.sofaMessage = sofaMessage;
        this.action = action;
    }

    public User getReceiver() {
        return receiver;
    }

    public SofaMessage getSofaMessage() {
        return sofaMessage;
    }

    public int getAction() {
        return action;
    }
}
