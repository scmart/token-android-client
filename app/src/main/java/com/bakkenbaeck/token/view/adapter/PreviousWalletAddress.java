package com.bakkenbaeck.token.view.adapter;

import android.content.Context;
import android.content.SharedPreferences;

import com.bakkenbaeck.token.view.BaseApplication;

public final class PreviousWalletAddress {

    private static final String WALLET_ADDRESSES = "a";

    private String address;
    private final SharedPreferences prefs;

    public PreviousWalletAddress() {
        this.prefs = BaseApplication.get().getSharedPreferences("pwa", Context.MODE_PRIVATE);
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
