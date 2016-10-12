package com.bakkenbaeck.token.view.adapter;

import android.content.SharedPreferences;

import com.bakkenbaeck.token.view.BaseApplication;
import com.securepreferences.SecurePreferences;

public final class PreviousWalletAddress {

    private static final String WALLET_ADDRESSES = "a";

    private String address;
    private final SharedPreferences prefs;

    public PreviousWalletAddress() {
        this.prefs = new SecurePreferences(BaseApplication.get(), "", "waa");
        loadFromPreferences();
    }

    private void loadFromPreferences() {
        this.address = this.prefs.getString(WALLET_ADDRESSES, null);
    }
    public void setAddress(final String address) {
        if (address.equals(this.address)) {
            return;
        }

        this.prefs
                .edit()
                .putString(WALLET_ADDRESSES, address)
                .apply();
        this.address = address;
    }

    public String getAddress() {
        return this.address;
    }
}
