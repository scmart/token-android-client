package com.bakkenbaeck.toshi.model;

import android.support.annotation.IntDef;

import io.realm.RealmObject;
import io.realm.annotations.Ignore;

public class ChatMessage extends RealmObject {

    @IntDef({
            TYPE_LOCAL_TEXT,
            TYPE_REMOTE_TEXT,
            TYPE_REMOTE_VIDEO
    })
    private @interface Type {}
    @Ignore public static final int TYPE_LOCAL_TEXT = 0;
    @Ignore public static final int TYPE_REMOTE_TEXT = 1;
    @Ignore public static final int TYPE_REMOTE_VIDEO = 2;

    private long creationTime;
    private @Type int type;
    private String text;
    private boolean hasBeenWatched = false;

    public ChatMessage() {
        this.creationTime = System.currentTimeMillis();
    }

    private ChatMessage setType(final @Type int type) {
        this.type = type;
        return this;
    }

    public ChatMessage setText(final String text) {
        this.text = text;
        return this;
    }

    public String getText() {
        return this.text;
    }

    @Type
    public int getType() {
        return this.type;
    }

    public void markAsWatched() {
        this.hasBeenWatched = true;
    }

    public boolean hasBeenWatched() {
        return hasBeenWatched;
    }


    // Helper functions

    public ChatMessage makeLocalMessageWithText(final String text) {
        setType(TYPE_LOCAL_TEXT);
        setText(text);
        return this;
    }

    public ChatMessage makeRemoteMessageWithText(final String text) {
        setType(TYPE_REMOTE_TEXT);
        setText(text);
        return this;
    }

    public ChatMessage makeRemoteVideoMessage() {
        setType(TYPE_REMOTE_VIDEO);
        setText("");
        return this;
    }
}
