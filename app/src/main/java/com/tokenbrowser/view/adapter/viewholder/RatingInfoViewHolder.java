package com.tokenbrowser.view.adapter.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.tokenbrowser.token.R;
import com.tokenbrowser.view.custom.RatingBar;

public class RatingInfoViewHolder extends RecyclerView.ViewHolder {

    private TextView stars;
    private RatingBar ratingBar;

    public RatingInfoViewHolder(View itemView) {
        super(itemView);

        this.stars = (TextView) itemView.findViewById(R.id.stars);
        this.ratingBar = (RatingBar) itemView.findViewById(R.id.rating_bar);
    }

    public void setStars(final String stars) {
        this.stars.setText(stars);
    }

    public void setPercentageRating(final int percentage) {
        this.ratingBar.setPercentage(percentage);
    }
}
