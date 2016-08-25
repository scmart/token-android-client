package com.bakkenbaeck.toshi.model;

import android.os.Parcel;
import android.os.Parcelable;

import io.realm.RealmObject;

public class RemoteTextMessage extends RealmObject implements ChatMessage, Parcelable {

    private String text;
    private long creationTime;

    public RemoteTextMessage() {
        this.creationTime = System.currentTimeMillis();
    }

    public final RemoteTextMessage setText(final String text) {
        this.text = text;
        return this;
    }

    @Override
    public final String getTextContents() {
        return this.text;
    }

    @Override
    public final @ChatMessage.Type int getType() {
        return TYPE_REMOTE_TEXT;
    }


    // Parcelable implementation
    private RemoteTextMessage(Parcel in) {
        text = in.readString();
        creationTime = in.readLong();
    }

    @Override
    public final void writeToParcel(Parcel dest, int flags) {
        dest.writeString(text);
        dest.writeLong(creationTime);
    }

    @Override
    public final int describeContents() {
        return 0;
    }

    public static final Creator<RemoteTextMessage> CREATOR = new Creator<RemoteTextMessage>() {
        @Override
        public RemoteTextMessage createFromParcel(Parcel in) {
            return new RemoteTextMessage(in);
        }

        @Override
        public RemoteTextMessage[] newArray(int size) {
            return new RemoteTextMessage[size];
        }
    };

}
