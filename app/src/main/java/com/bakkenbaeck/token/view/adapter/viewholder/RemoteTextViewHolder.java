package com.bakkenbaeck.token.view.adapter.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.bakkenbaeck.token.R;

public final class RemoteTextViewHolder extends RecyclerView.ViewHolder {
    public TextView messageText;


    public RemoteTextViewHolder(final View v) {
        super(v);
        this.messageText = (TextView) v.findViewById(R.id.single_message_item__message);
    }
}
