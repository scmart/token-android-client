package com.bakkenbaeck.token.view.adapter.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.bakkenbaeck.token.R;

public final class LocalPaymentRequestViewHolder extends RecyclerView.ViewHolder {

    public TextView messageText;
    public TextView requestedAmount;
    public TextView secondaryAmount;

    public LocalPaymentRequestViewHolder(final View v) {
        super(v);
        this.messageText = (TextView) v.findViewById(R.id.message);
        this.requestedAmount = (TextView) v.findViewById(R.id.requested_amount);
        this.secondaryAmount = (TextView) v.findViewById(R.id.eth_amount);

    }
}
