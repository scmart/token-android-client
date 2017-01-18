package com.bakkenbaeck.token.model.local;


import android.support.annotation.IntDef;

public class SendState {

    @IntDef({
            STATE_SENDING,
            STATE_SENT,
            STATE_FAILED,
            STATE_RECEIVED
    })
    public @interface State {}

    public static final int STATE_SENDING = 0;
    public static final int STATE_SENT = 1;
    public static final int STATE_FAILED = 2;
    public static final int STATE_RECEIVED = 3;
}
