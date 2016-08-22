package com.bakkenbaeck.toshi.model;

import android.os.Parcel;
import android.os.Parcelable;

public final class RemoteWithdrawMessage extends Message implements Parcelable {

    public RemoteWithdrawMessage() {}

    @Override
    public String getTextContents() {
        return "";
    }

    @Override
    public @Type int getType() {
        return TYPE_REMOTE_WITHDRAW;
    }


    // Parcelable implementation
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(final Parcel out, final int flags) {
    }

    public static final Creator<RemoteWithdrawMessage> CREATOR = new Creator<RemoteWithdrawMessage>() {
        public RemoteWithdrawMessage createFromParcel(final Parcel in) {
            return new RemoteWithdrawMessage(in);
        }

        public RemoteWithdrawMessage[] newArray(final int size) {
            return new RemoteWithdrawMessage[size];
        }
    };

    private RemoteWithdrawMessage(final Parcel in) {
    }
}
