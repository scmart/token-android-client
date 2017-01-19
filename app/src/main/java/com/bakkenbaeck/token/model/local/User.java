package com.bakkenbaeck.token.model.local;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Parcel;
import android.os.Parcelable;

import com.bakkenbaeck.token.R;
import com.bakkenbaeck.token.view.BaseApplication;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class User extends RealmObject implements Parcelable {

    @PrimaryKey
    private String owner_address;
    private String username;
    private CustomUserInformation custom;

    // ctors
    public User() {}

    private User(final Parcel in) {
        owner_address = in.readString();
        username = in.readString();
    }

    // Getters

    public String getUsername() {
        return username;
    }

    public String getAddress() {
        return owner_address;
    }

    public String getAbout() {
        return custom == null ? null : this.custom.getAbout();
    }

    public String getLocation() {
        return custom == null ? null : this.custom.getLocation();
    }

    public Bitmap getImage() {
        return BitmapFactory.decodeResource(BaseApplication.get().getResources(), R.mipmap.launcher);
    }


    // Parcelable implementation
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        dest.writeString(owner_address);
        dest.writeString(username);
        dest.writeParcelable(custom, flags);
    }
}
