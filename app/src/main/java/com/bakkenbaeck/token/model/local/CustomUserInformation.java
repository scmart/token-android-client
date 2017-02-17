package com.bakkenbaeck.token.model.local;


import android.os.Parcel;
import android.os.Parcelable;

import io.realm.RealmObject;

public class CustomUserInformation extends RealmObject implements Parcelable {
    private String about;
    private String location;
    private String name;
    private String payment_address;

    public CustomUserInformation() {}

    /* package */ CustomUserInformation(final CustomUserInformation customUserInformation) {
        this.about = customUserInformation.getAbout();
        this.location = customUserInformation.getLocation();
        this.name = customUserInformation.getName();
        this.payment_address = customUserInformation.getPaymentAddress();
    }

    private CustomUserInformation(Parcel in) {
        about = in.readString();
        location = in.readString();
        name = in.readString();
        payment_address = in.readString();
    }

    public static final Creator<CustomUserInformation> CREATOR = new Creator<CustomUserInformation>() {
        @Override
        public CustomUserInformation createFromParcel(Parcel in) {
            return new CustomUserInformation(in);
        }

        @Override
        public CustomUserInformation[] newArray(int size) {
            return new CustomUserInformation[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        dest.writeString(about);
        dest.writeString(location);
        dest.writeString(name);
        dest.writeString(payment_address);
    }

    public String getAbout() {
        return this.about;
    }

    public String getLocation() {
        return this.location;
    }

    public String getName() {
        return this.name;
    }

    public String getPaymentAddress() {
        return this.payment_address;
    }
}