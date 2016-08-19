package com.bakkenbaeck.toshi.view.adapter.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.bakkenbaeck.toshi.R;

public final class WalletAddressViewHolder extends RecyclerView.ViewHolder {
    public TextView addressText;

    public WalletAddressViewHolder(final View v) {
        super(v);
        this.addressText = (TextView) v.findViewById(R.id.addressText);
    }
}
