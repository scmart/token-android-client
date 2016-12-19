package com.bakkenbaeck.token.model;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

public class Contact {

    private String name;
    private String base64Avatar;

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
}
