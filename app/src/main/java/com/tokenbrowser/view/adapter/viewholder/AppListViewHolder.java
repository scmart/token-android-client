package com.tokenbrowser.view.adapter.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.tokenbrowser.token.R;
import com.tokenbrowser.model.network.App;
import com.tokenbrowser.view.adapter.listeners.OnItemClickListener;
import com.bumptech.glide.Glide;

public class AppListViewHolder extends RecyclerView.ViewHolder {
    private ImageView appImage;
    private TextView appName;

    public AppListViewHolder(View itemView) {
        super(itemView);

        this.appImage = (ImageView) itemView.findViewById(R.id.app_image);
        this.appName = (TextView) itemView.findViewById(R.id.app_name);
    }

    public void setApp(final App app) {
        this.appName.setText(app.getDisplayName());
        Glide.with(this.appImage.getContext())
                .load(app.getAvatarUrl())
                .into(this.appImage);
    }

    public void bind(final App app, final OnItemClickListener<App> listener) {
        this.itemView.setOnClickListener(view -> {
            if (listener == null) {
                return;
            }

            listener.onItemClick(app);
        });
    }
}
