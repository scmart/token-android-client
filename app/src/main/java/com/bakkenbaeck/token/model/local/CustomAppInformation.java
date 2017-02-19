package com.bakkenbaeck.token.model.local;

import android.os.Parcel;
import android.os.Parcelable;

import com.bakkenbaeck.token.model.network.App;

import java.util.List;

import io.realm.RealmList;
import io.realm.RealmObject;

public class CustomAppInformation extends RealmObject implements Parcelable {
    private String paymentAddress;
    private String webApp;
    private String displayName;
    private String protocol;
    private String avatarUrl;
    private RealmList<RealmString> languages;
    private RealmList<RealmString> interfaces;

    public CustomAppInformation() {}

    public CustomAppInformation(final App app) {
        this.paymentAddress = app.getPaymentAddress();
        this.webApp = app.getWebApp();
        this.displayName = app.getDisplayName();
        this.protocol = app.getProtocol();
        this.avatarUrl = app.getAvatarUrl();
        this.languages = toRealmList(app.getLanguages());
        this.interfaces = toRealmList(app.getInterfaces());
    }

    protected CustomAppInformation(Parcel in) {
        paymentAddress = in.readString();
        webApp = in.readString();
        displayName = in.readString();
        protocol = in.readString();
        avatarUrl = in.readString();
        languages = readParcel(in);
        interfaces = readParcel(in);
    }

    @Override
    public void writeToParcel(Parcel dest, final int flags) {
        dest.writeString(paymentAddress);
        dest.writeString(webApp);
        dest.writeString(displayName);
        dest.writeString(protocol);
        dest.writeString(avatarUrl);
        writeList(dest, languages, flags);
        writeList(dest, interfaces, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<CustomAppInformation> CREATOR = new Creator<CustomAppInformation>() {
        @Override
        public CustomAppInformation createFromParcel(Parcel in) {
            return new CustomAppInformation(in);
        }

        @Override
        public CustomAppInformation[] newArray(int size) {
            return new CustomAppInformation[size];
        }
    };

    private void writeList(final Parcel parcel, final RealmList<RealmString> inList, final int flag) {
        for (final RealmString s : inList) {
            parcel.writeParcelable(s, flag);
        }
    }

    private RealmList<RealmString> readParcel(final Parcel parcel) {
        final int size = parcel.readInt();
        final RealmList<RealmString> list = new RealmList<>();

        for (int i = 0; i < size; i++) {
            final RealmString rs = parcel.readParcelable(getClass().getClassLoader());
            list.add(rs);
        }

        return list;
    }

    private RealmList<RealmString> toRealmList(final List<String> list) {
        final RealmList<RealmString> rs = new RealmList<>();
        list.forEach((s -> rs.add(new RealmString(s))));

        return rs;
    }
}
