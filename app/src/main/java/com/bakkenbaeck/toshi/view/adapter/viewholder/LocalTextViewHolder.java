package com.bakkenbaeck.toshi.view.adapter.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.bakkenbaeck.toshi.R;

public final class LocalTextViewHolder extends RecyclerView.ViewHolder {
    public TextView messageText;

    public LocalTextViewHolder(final View v) {
        super(v);
        this.messageText = (TextView) v.findViewById(R.id.single_message_item__message);
    }
}
