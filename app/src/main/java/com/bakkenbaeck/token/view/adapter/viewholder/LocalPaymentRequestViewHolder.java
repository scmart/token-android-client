package com.bakkenbaeck.token.view.adapter.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.bakkenbaeck.token.R;
import com.bakkenbaeck.token.model.sofa.TxRequest;

public final class LocalPaymentRequestViewHolder extends RecyclerView.ViewHolder {

    private View localView;
    private View remoteView;
    private TextView localMessageText;
    private TextView remoteMessageText;
    private TextView localRequestedAmount;
    private TextView remoteRequestedAmount;
    private TextView localSecondaryAmount;
    private TextView remoteSecondaryAmount;

    public LocalPaymentRequestViewHolder(final View v) {
        super(v);
        this.localView = v.findViewById(R.id.local);
        this.remoteView = v.findViewById(R.id.remote);
        this.localMessageText = (TextView) v.findViewById(R.id.local_message);
        this.remoteMessageText = (TextView) v.findViewById(R.id.remote_message);
        this.localRequestedAmount = (TextView) v.findViewById(R.id.local_requested_amount);
        this.remoteRequestedAmount = (TextView) v.findViewById(R.id.remote_requested_amount);
        this.localSecondaryAmount = (TextView) v.findViewById(R.id.local_eth_amount);
        this.remoteSecondaryAmount = (TextView) v.findViewById(R.id.remote_eth_amount);

    }

    public void setTxRequest(final TxRequest request, final boolean sentByLocal) {
        if (sentByLocal) {
            this.localView.setVisibility(View.VISIBLE);
            this.remoteView.setVisibility(View.GONE);
            this.localRequestedAmount.setText(request.getValue() + " " + request.getCurrency());
            this.localSecondaryAmount.setText(" · 0.0000 ETH");
        } else {
            this.remoteView.setVisibility(View.VISIBLE);
            this.localView.setVisibility(View.GONE);
            this.remoteRequestedAmount.setText(request.getValue() + " " + request.getCurrency());
            this.remoteSecondaryAmount.setText(" · 0.0000 ETH");
        }
    }
}
