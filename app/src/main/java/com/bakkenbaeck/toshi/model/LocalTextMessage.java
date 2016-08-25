package com.bakkenbaeck.toshi.model;

import android.support.annotation.NonNull;

import io.realm.RealmObject;

public class LocalTextMessage extends RealmObject implements ChatMessage {

    private String message;
    @SuppressWarnings("FieldCanBeLocal")
    private long creationTime;

    public LocalTextMessage() {
        this.creationTime = System.currentTimeMillis();
    }

    public final LocalTextMessage setMessage(@NonNull final String message) {
        this.message = message;
        return this;
    }

    @Override
    public final String getTextContents() {
        return this.message;
    }

    @Override
    public final int getType() {
        return TYPE_LOCAL_TEXT;
    }
}
