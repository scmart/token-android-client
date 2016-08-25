package com.bakkenbaeck.toshi.model;

import android.os.Parcel;
import android.os.Parcelable;

import io.realm.RealmObject;

public class RemoteWithdrawMessage extends RealmObject implements ChatMessage, Parcelable {

    private long creationTime;

    public RemoteWithdrawMessage() {
        this.creationTime = System.currentTimeMillis();
    }

    @Override
    public final String getTextContents() {
        return "";
    }

    @Override
    public final @Type int getType() {
        return TYPE_REMOTE_WITHDRAW;
    }


    // Parcelable implementation
    private RemoteWithdrawMessage(final Parcel in) {
        this.creationTime = in.readLong();
    }

    @Override
    public final void writeToParcel(final Parcel dest, final int flags) {
        dest.writeLong(creationTime);
    }

    @Override
    public final int describeContents() {
        return 0;
    }

    public static final Creator<RemoteWithdrawMessage> CREATOR = new Creator<RemoteWithdrawMessage>() {
        @Override
        public RemoteWithdrawMessage createFromParcel(final Parcel in) {
            return new RemoteWithdrawMessage(in);
        }

        @Override
        public RemoteWithdrawMessage[] newArray(final int size) {
            return new RemoteWithdrawMessage[size];
        }
    };
}
