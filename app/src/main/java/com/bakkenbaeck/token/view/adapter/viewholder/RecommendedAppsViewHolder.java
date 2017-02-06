package com.bakkenbaeck.token.view.adapter.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.bakkenbaeck.token.R;

public class RecommendedAppsViewHolder extends RecyclerView.ViewHolder {
    private TextView appLabel;
    private TextView appCategory;

    public RecommendedAppsViewHolder(View itemView) {
        super(itemView);

        this.appLabel = (TextView) itemView.findViewById(R.id.app_label);
        this.appCategory = (TextView) itemView.findViewById(R.id.app_category);
    }

    public void setLabel(final String label) {
        this.appLabel.setText(label);
    }

    public void setCategory(final String category) {
        this.appCategory.setText(category);
    }
}
