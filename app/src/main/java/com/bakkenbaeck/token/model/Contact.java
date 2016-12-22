package com.bakkenbaeck.token.model;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Base64;

public class Contact implements Parcelable {

    private String name;
    private String base64Avatar;

    public Contact() {}

    private Contact(final Parcel in) {
        this.name = in.readString();
        this.base64Avatar = in.readString();
    }

    public static final Creator<Contact> CREATOR = new Creator<Contact>() {
        @Override
        public Contact createFromParcel(final Parcel in) {
            return new Contact(in);
        }

        @Override
        public Contact[] newArray(final int size) {
            return new Contact[size];
        }
    };

    public Contact setName(final String name) {
        this.name = name;
        return this;
    }

    public Contact setBase64EncodedAvatar(final String base64Avatar) {
        this.base64Avatar = base64Avatar;
        return this;
    }

    public String getName() {
        return this.name;
    }

    public Bitmap getImage() {
        final byte[] decoded = Base64.decode(this.base64Avatar, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        dest.writeString(name);
        dest.writeString(base64Avatar);
    }

    public String getConversationId() {
        return "0xa2a0134f1df987bc388dbcb635dfeed4ce497e2a";
    }
}
