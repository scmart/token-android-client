package com.bakkenbaeck.token.view.adapter.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bakkenbaeck.token.R;
import com.bakkenbaeck.token.model.network.App;
import com.bakkenbaeck.token.view.adapter.listeners.OnItemClickListener;
import com.bakkenbaeck.token.view.custom.StarRatingView;
import com.bumptech.glide.Glide;

public class SearchAppViewHolder extends RecyclerView.ViewHolder {
    private TextView appLabel;
    private TextView appCategory;
    private StarRatingView ratingView;
    private ImageView appImage;

    public SearchAppViewHolder(View itemView) {
        super(itemView);

        this.appLabel = (TextView) itemView.findViewById(R.id.app_label);
        this.appCategory = (TextView) itemView.findViewById(R.id.app_category);
        this.ratingView = (StarRatingView) itemView.findViewById(R.id.rating_view);
        this.appImage = (ImageView) itemView.findViewById(R.id.app_image);
    }

    public void setLabel(final App app) {
        this.appLabel.setText(app.getDisplayName());
    }

    public void setCategory(final App app) {
        this.appCategory.setText(app.getInterfaces().get(0));
    }

    public void setImage(final App app) {
        Glide.with(this.appImage.getContext())
                .load(app.getAvatarUrl())
                .into(this.appImage);
    }

    public void setRating(final double rating) {
        this.ratingView.setStars(rating);
    }

    public void bind(final App app, final OnItemClickListener<App> listener) {
        this.itemView.setOnClickListener(view -> {
            listener.onItemClick(app);
        });
    }
}
