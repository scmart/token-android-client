package com.tokenbrowser.model.local;

import android.os.Parcel;
import android.os.Parcelable;

import io.realm.RealmObject;

public class RealmString extends RealmObject implements Parcelable {
    private String value;

    public RealmString() {}

    public RealmString(final String s) {
        this.value = s;
    }

    protected RealmString(Parcel in) {
        value = in.readString();
    }

    public static final Creator<RealmString> CREATOR = new Creator<RealmString>() {
        @Override
        public RealmString createFromParcel(Parcel in) {
            return new RealmString(in);
        }

        @Override
        public RealmString[] newArray(int size) {
            return new RealmString[size];
        }
    };

    public String getValue() {
        return value;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(value);
    }
}
