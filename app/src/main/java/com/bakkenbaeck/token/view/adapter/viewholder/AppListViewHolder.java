package com.bakkenbaeck.token.view.adapter.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bakkenbaeck.token.R;

public class AppListViewHolder extends RecyclerView.ViewHolder {
    private ImageView appImage;
    private TextView appName;

    public AppListViewHolder(View itemView) {
        super(itemView);

        this.appImage = (ImageView) itemView.findViewById(R.id.app_image);
        this.appName = (TextView) itemView.findViewById(R.id.app_name);
    }

    public void setAppName(final String appName) {
        this.appName.setText(appName);
    }

    public void setAppImage(final int imageRef) {
        this.appImage.setImageResource(imageRef);
    }
}
