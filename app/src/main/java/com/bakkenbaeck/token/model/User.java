package com.bakkenbaeck.token.model;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Base64;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class User extends RealmObject implements Parcelable {

    @PrimaryKey
    private String owner_address;
    private String username;
    private String base64Avatar;

    public User() {}

    private User(final Parcel in) {
        owner_address = in.readString();
        username = in.readString();
        base64Avatar = in.readString();
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    public String getUsername() {
        return username;
    }

    public String getAddress() {
        return owner_address;
    }

    public Bitmap getImage() {
        if (this.base64Avatar == null) {
            return null;
        }

        final byte[] decoded = Base64.decode(this.base64Avatar, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        dest.writeString(owner_address);
        dest.writeString(username);
        dest.writeString(base64Avatar);
    }
}
