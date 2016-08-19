package com.bakkenbaeck.toshi.model;

import android.os.Parcel;
import android.os.Parcelable;

public final class RemoteVideoMessage extends Message implements Parcelable {

    private boolean hasBeenViewed;

    public RemoteVideoMessage() {}

    @Override
    public String getTextContents() {
        return "";
    }

    @Override
    public @Type int getType() {
        return TYPE_REMOTE_VIDEO;
    }

    public boolean hasBeenViewed() {
        return hasBeenViewed;
    }

    // Parcelable implementation

    protected RemoteVideoMessage(Parcel in) {
        hasBeenViewed = in.readByte() != 0;
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(final Parcel parcel, final int i) {
        parcel.writeByte((byte) (hasBeenViewed ? 1 : 0));
    }

    public void markAsWatched() {
        this.hasBeenViewed = true;
    }
}
