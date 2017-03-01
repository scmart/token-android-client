package com.tokenbrowser.view.adapter.viewholder;


import android.support.v7.widget.RecyclerView;
import android.view.View;

public class ClickableViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    public interface OnClickListener {
        void onClick(int position);
    }

    private OnClickListener onClickListener;

    public ClickableViewHolder(final View itemView) {
        super(itemView);
        itemView.setOnClickListener(this);
    }


    @Override
    public void onClick(final View v) {
        if (this.onClickListener != null) {
            this.onClickListener.onClick(getAdapterPosition());
        }
    }

    public void setOnClickListener(final OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }
}
