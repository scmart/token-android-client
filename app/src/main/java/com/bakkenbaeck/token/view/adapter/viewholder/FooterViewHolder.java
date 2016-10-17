package com.bakkenbaeck.token.view.adapter.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.FrameLayout;

public class FooterViewHolder extends RecyclerView.ViewHolder{
    FrameLayout footer;

    public FooterViewHolder(View itemView) {
        super(itemView);

        this.footer = (FrameLayout) itemView;
    }
}
