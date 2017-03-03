package com.tokenbrowser.view.adapter.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.tokenbrowser.token.R;
import com.tokenbrowser.model.network.App;
import com.tokenbrowser.view.adapter.listeners.OnItemClickListener;
import com.tokenbrowser.view.custom.StarRatingView;
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
        this.appLabel.setText(app.getName());
    }

    public void setCategory(final App app) {
        this.appCategory.setText(app.getManifest().getInterfaces().get(0));
    }

    public void setImage(final App app) {
        Glide.with(this.appImage.getContext())
                .load(app.getManifest().getAvatarUrl())
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
