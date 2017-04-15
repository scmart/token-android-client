package com.tokenbrowser.util;

import android.support.annotation.IntDef;

public class PaymentConfirmViewType {
    @IntDef({TOKEN, EXTERNAL})
    public @interface Type {}
    public static final int TOKEN = 1;
    public static final int EXTERNAL = 2;
}
