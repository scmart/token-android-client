package com.bakkenbaeck.token.model.local;

import android.os.Parcel;
import android.os.Parcelable;

public class Library implements Parcelable{
    private String name;
    private String licence;

    public Library() {}

    public String getName() {
        return name;
    }

    public String getLicence() {
        return licence;
    }

    public Library setName(String name) {
        this.name = name;
        return this;
    }

    public Library setLicence(String licence) {
        this.licence = licence;
        return this;
    }

    protected Library(Parcel in) {
        name = in.readString();
        licence = in.readString();
    }

    public static final Creator<Library> CREATOR = new Creator<Library>() {
        @Override
        public Library createFromParcel(Parcel in) {
            return new Library(in);
        }

        @Override
        public Library[] newArray(int size) {
            return new Library[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(name);
        parcel.writeString(licence);
    }
}
