package com.tokenbrowser.view.adapter.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.tokenbrowser.R;
import com.tokenbrowser.model.network.App;
import com.tokenbrowser.util.ImageUtil;
import com.tokenbrowser.view.adapter.listeners.OnItemClickListener;
import com.tokenbrowser.view.custom.StarRatingView;

public class RecommendedAppsViewHolder extends RecyclerView.ViewHolder {
    private TextView appLabel;
    private TextView appCategory;
    private StarRatingView ratingView;
    private ImageView appImage;

    public RecommendedAppsViewHolder(View itemView) {
        super(itemView);

        this.appLabel = (TextView) itemView.findViewById(R.id.app_label);
        this.appCategory = (TextView) itemView.findViewById(R.id.app_category);
        this.ratingView = (StarRatingView) itemView.findViewById(R.id.rating_view);
        this.appImage = (ImageView) itemView.findViewById(R.id.app_image);
    }

    public void setApp(final App app) {
        this.appLabel.setText(app.getCustom().getName());
    }

    public void setCategory(final App app) {
        if (app == null) {
            return;
        }

        final double reputationScore = app.getReputationScore() != null
                ? app.getReputationScore()
                : 0;
        this.ratingView.setStars(reputationScore);

        ImageUtil.loadFromNetwork(app.getCustom().getAvatar(), this.appImage);
    }

    public void bind(final App app, OnItemClickListener<App> listener) {
        this.itemView.setOnClickListener(view -> listener.onItemClick(app));
    }
}
