package com.tokenbrowser.view.adapter.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.tokenbrowser.model.network.App;
import com.tokenbrowser.R;
import com.tokenbrowser.view.BaseApplication;
import com.tokenbrowser.view.adapter.listeners.OnItemClickListener;
import com.tokenbrowser.view.custom.StarRatingView;

public class SearchAppViewHolder extends RecyclerView.ViewHolder {
    private TextView appLabel;
    private TextView appCategory;
    private StarRatingView ratingView;
    private ImageView appImage;
    private TextView reviewCount;

    public SearchAppViewHolder(View itemView) {
        super(itemView);

        this.appLabel = (TextView) itemView.findViewById(R.id.app_label);
        this.appCategory = (TextView) itemView.findViewById(R.id.app_category);
        this.ratingView = (StarRatingView) itemView.findViewById(R.id.rating_view);
        this.appImage = (ImageView) itemView.findViewById(R.id.app_image);
        this.reviewCount = (TextView) itemView.findViewById(R.id.review_count);
    }

    public void setApp(final App app) {
        this.appLabel.setText(app.getCustom().getName());
        final double reputationScore = app.getReputationScore() != null
                ? app.getReputationScore()
                : 0;
        this.ratingView.setStars(reputationScore);
        final String reviewCount = BaseApplication.get().getString(R.string.parentheses, app.getReviewCount());
        this.reviewCount.setText(reviewCount);

        Glide.with(this.appImage.getContext())
                .load(app.getCustom().getAvatar())
                .into(this.appImage);
    }

    public void bind(final App app, final OnItemClickListener<App> listener) {
        this.itemView.setOnClickListener(view -> {
            listener.onItemClick(app);
        });
    }
}
