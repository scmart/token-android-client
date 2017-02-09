package com.bakkenbaeck.token.view.adapter.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import com.bakkenbaeck.token.R;

public class StarViewHolder extends RecyclerView.ViewHolder {
    private ImageView imageView;

    public StarViewHolder(View itemView) {
        super(itemView);

        imageView = (ImageView) itemView.findViewById(R.id.star);
    }

    public void setWholeStar() {
        imageView.setImageResource(R.drawable.star);
    }

    public void setHalfStar() {
        imageView.setImageResource(R.drawable.star_half);
    }

    public void setWholeGreyStar() {
        imageView.setImageResource(R.drawable.star_grey);
    }
}
