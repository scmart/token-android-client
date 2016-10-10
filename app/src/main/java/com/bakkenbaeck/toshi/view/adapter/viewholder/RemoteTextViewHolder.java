package com.bakkenbaeck.toshi.view.adapter.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bakkenbaeck.toshi.R;

public final class RemoteTextViewHolder extends RecyclerView.ViewHolder {
    public TextView messageText;
    public LinearLayout details;
    public TextView earned;
    public TextView earnedValue;
    public TextView earnedTotal;
    public TextView earnedTotalValue;

    public RemoteTextViewHolder(final View v) {
        super(v);
        this.messageText = (TextView) v.findViewById(R.id.single_message_item__message);
        this.details = (LinearLayout) v.findViewById(R.id.details);
        this.earned = (TextView) v.findViewById(R.id.earned);
        this.earnedValue = (TextView) v.findViewById(R.id.earnedValue);
        this.earnedTotal = (TextView) v.findViewById(R.id.earnedTotal);
        this.earnedTotalValue = (TextView) v.findViewById(R.id.earnedTotalValue);
    }
}
