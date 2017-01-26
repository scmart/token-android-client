package com.bakkenbaeck.token.view.adapter.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.bakkenbaeck.token.R;

public class ControlGroupPopupHolder extends RecyclerView.ViewHolder {
    private TextView label;

    public ControlGroupPopupHolder(View itemView) {
        super(itemView);
        this.label = (TextView) itemView.findViewById(R.id.label);
    }

    public void setText(final String text) {
        this.label.setText(text);
    }
}