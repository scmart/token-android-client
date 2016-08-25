package com.bakkenbaeck.toshi.model;

import android.support.annotation.IntDef;

public interface ChatMessage {

    @IntDef({
            TYPE_LOCAL_TEXT,
            TYPE_REMOTE_TEXT,
            TYPE_REMOTE_VIDEO,
            TYPE_REMOTE_WITHDRAW
    })
    @interface Type {}
    int TYPE_LOCAL_TEXT = 0;
    int TYPE_REMOTE_TEXT = 1;
    int TYPE_REMOTE_VIDEO = 2;
    int TYPE_REMOTE_WITHDRAW = 3;

    String getTextContents();
    @Type
    int getType();
}
