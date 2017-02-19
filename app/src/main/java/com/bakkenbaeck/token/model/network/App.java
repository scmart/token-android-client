package com.bakkenbaeck.token.model.network;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

public class App implements Parcelable {
    private String displayName;
    private String protocol;
    private String webApp;
    private List<String> languages;
    private List<String> interfaces;
    private String avatarUrl;
    private String ethereumAddress;

    protected App(Parcel in) {
        displayName = in.readString();
        protocol = in.readString();
        webApp = in.readString();
        languages = in.createStringArrayList();
        interfaces = in.createStringArrayList();
        avatarUrl = in.readString();
        ethereumAddress = in.readString();
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public List<String> getInterfaces() {
        return interfaces;
    }

    public static final Creator<App> CREATOR = new Creator<App>() {
        @Override
        public App createFromParcel(Parcel in) {
            return new App(in);
        }

        @Override
        public App[] newArray(int size) {
            return new App[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(displayName);
        parcel.writeString(protocol);
        parcel.writeString(webApp);
        parcel.writeStringList(languages);
        parcel.writeStringList(interfaces);
        parcel.writeString(avatarUrl);
        parcel.writeString(ethereumAddress);
    }
}
