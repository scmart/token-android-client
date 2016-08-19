package com.bakkenbaeck.toshi.model;

import android.support.annotation.NonNull;

public final class LocalTextMessage extends Message {
    private String message;

    public final LocalTextMessage setMessage(@NonNull final String message) {
        this.message = message;
        return this;
    }

    @Override
    public String getTextContents() {
        return this.message;
    }

    @Override
    public int getType() {
        return TYPE_LOCAL_TEXT;
    }
}
