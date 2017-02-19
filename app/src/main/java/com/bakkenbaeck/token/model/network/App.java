package com.bakkenbaeck.token.model.network;

import android.os.Parcel;
import android.os.Parcelable;

import com.bakkenbaeck.token.model.sofa.*;

import java.util.List;

public class App implements Parcelable {
    private List<String> languages;
    private String paymentAddress;
    private String webApp;
    private String displayName;
    private List<String> interfaces;
    private String protocol;
    private String avatarUrl;
    private String ownerAddress;
    private InitRequest initRequest;

    protected App(Parcel in) {
        languages = in.createStringArrayList();
        paymentAddress = in.readString();
        webApp = in.readString();
        displayName = in.readString();
        interfaces = in.createStringArrayList();
        protocol = in.readString();
        avatarUrl = in.readString();
        ownerAddress = in.readString();
        initRequest = in.readParcelable(InitRequest.class.getClassLoader());
    }

    public List<String> getLanguages() {
        return languages;
    }

    public String getPaymentAddress() {
        return paymentAddress;
    }

    public String getWebApp() {
        return webApp;
    }

    public String getDisplayName() {
        return displayName;
    }

    public List<String> getInterfaces() {
        return interfaces;
    }

    public String getProtocol() {
        return protocol;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public String getOwnerAddress() {
        return ownerAddress;
    }

    public InitRequest getInitRequest() {
        return initRequest;
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
        parcel.writeStringList(languages);
        parcel.writeString(paymentAddress);
        parcel.writeString(webApp);
        parcel.writeString(displayName);
        parcel.writeStringList(interfaces);
        parcel.writeString(protocol);
        parcel.writeString(avatarUrl);
        parcel.writeString(ownerAddress);
        parcel.writeParcelable(initRequest, i);
    }
}
