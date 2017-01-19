package com.bakkenbaeck.token.view.adapter.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.bakkenbaeck.token.R;

public final class TextViewHolder extends RecyclerView.ViewHolder {

    private TextView localText;
    private TextView remoteText;


    public TextViewHolder(final View v) {
        super(v);
        this.localText = (TextView) v.findViewById(R.id.local_message);
        this.remoteText = (TextView) v.findViewById(R.id.remote_message);
    }

    public void setText(final String text, final boolean isLocal) {
        if (isLocal) {
            this.remoteText.setVisibility(View.GONE);
            this.localText.setVisibility(View.VISIBLE);
            this.localText.setText(text);
        } else {
            this.localText.setVisibility(View.GONE);
            this.remoteText.setVisibility(View.VISIBLE);
            this.remoteText.setText(text);
        }
    }
}
