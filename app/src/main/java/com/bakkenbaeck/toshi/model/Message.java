package com.bakkenbaeck.toshi.model;

import android.support.annotation.IntDef;

public abstract class Message {

    @IntDef({
            TYPE_LOCAL_TEXT,
            TYPE_REMOTE_TEXT,
            TYPE_REMOTE_VIDEO
    })
    public @interface Type {}
    public static final int TYPE_LOCAL_TEXT = 0;
    public static final int TYPE_REMOTE_TEXT = 1;
    public static final int TYPE_REMOTE_VIDEO = 2;

    public abstract String getTextContents();
    @Type
    public abstract int getType();
}
