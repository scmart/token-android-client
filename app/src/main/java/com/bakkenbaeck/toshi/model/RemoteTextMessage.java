package com.bakkenbaeck.toshi.model;

import android.os.Parcel;
import android.os.Parcelable;

public final class RemoteTextMessage extends Message implements Parcelable {

    private String text;

    public RemoteTextMessage() {}

    public RemoteTextMessage setText(final String text) {
        this.text = text;
        return this;
    }

    @Override
    public String getTextContents() {
        return this.text;
    }

    @Override
    public @Message.Type int getType() {
        return TYPE_REMOTE_TEXT;
    }


    // Parcelable implementation
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(final Parcel out, final int flags) {
        out.writeString(text);
    }

    public static final Parcelable.Creator<RemoteTextMessage> CREATOR = new Parcelable.Creator<RemoteTextMessage>() {
        public RemoteTextMessage createFromParcel(final Parcel in) {
            return new RemoteTextMessage(in);
        }

        public RemoteTextMessage[] newArray(final int size) {
            return new RemoteTextMessage[size];
        }
    };

    private RemoteTextMessage(final Parcel in) {
        text = in.readString();
    }
}
