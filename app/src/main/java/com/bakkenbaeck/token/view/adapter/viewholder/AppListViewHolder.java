package com.bakkenbaeck.token.view.adapter.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bakkenbaeck.token.R;
import com.bakkenbaeck.token.view.adapter.listeners.OnItemClickListener;

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

    public void bind(final int position, final OnItemClickListener listener) {
        this.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener == null) {
                    return;
                }

                listener.onItemClick(position);
            }
        });
    }
}
