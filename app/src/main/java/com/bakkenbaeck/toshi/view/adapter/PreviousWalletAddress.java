package com.bakkenbaeck.toshi.view.adapter;

import android.content.SharedPreferences;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bakkenbaeck.toshi.R;
import com.bakkenbaeck.toshi.view.BaseApplication;
import com.bakkenbaeck.toshi.view.adapter.viewholder.WalletAddressViewHolder;
import com.securepreferences.SecurePreferences;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import rx.Observable;
import rx.subjects.PublishSubject;

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
