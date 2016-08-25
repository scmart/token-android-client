package com.bakkenbaeck.toshi.model;

import android.os.Parcel;
import android.os.Parcelable;

import io.realm.RealmObject;

public class RemoteVideoMessage extends RealmObject implements ChatMessage, Parcelable {

    private boolean hasBeenViewed;
    private long creationTime;

    public RemoteVideoMessage() {
        this.creationTime = System.currentTimeMillis();
    }

    @Override
    public final String getTextContents() {
        return "";
    }

    @Override
    public final @Type int getType() {
        return TYPE_REMOTE_VIDEO;
    }

    public final boolean hasBeenViewed() {
        return hasBeenViewed;
    }

    public final void markAsWatched() {
        this.hasBeenViewed = true;
    }

    // Parcelable implementation

    private RemoteVideoMessage(Parcel in) {
        hasBeenViewed = in.readByte() != 0;
        creationTime = in.readLong();
    }

    @Override
    public final void writeToParcel(Parcel dest, int flags) {
        dest.writeByte((byte) (hasBeenViewed ? 1 : 0));
        dest.writeLong(creationTime);
    }

    @Override
    public final int describeContents() {
        return 0;
    }

    public static final Creator<RemoteVideoMessage> CREATOR = new Creator<RemoteVideoMessage>() {
        @Override
        public RemoteVideoMessage createFromParcel(Parcel in) {
            return new RemoteVideoMessage(in);
        }

        @Override
        public RemoteVideoMessage[] newArray(int size) {
            return new RemoteVideoMessage[size];
        }
    };


}
