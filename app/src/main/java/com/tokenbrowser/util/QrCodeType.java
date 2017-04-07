package com.tokenbrowser.util;

import android.support.annotation.IntDef;

public class QrCodeType {
    @IntDef({ADD, PAY, EXTERNAL, INVALID})
    public @interface Type {}
    public static final int ADD = 1;
    public static final int PAY = 2;
    public static final int EXTERNAL = 3;
    public static final int INVALID = 4;
}
