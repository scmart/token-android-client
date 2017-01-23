package com.bakkenbaeck.token.view.adapter.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

public class ControlViewHolder extends RecyclerView.ViewHolder {
    private TextView item;

    public ControlViewHolder(View itemView) {
        super(itemView);
        this.item = (TextView) itemView;
    }

    public void setText(final String text) {
        this.item.setText(text);
    }
}
