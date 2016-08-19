package com.bakkenbaeck.toshi.view.adapter.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.bakkenbaeck.toshi.R;

public final class RemoteVideoViewHolder extends RecyclerView.ViewHolder {

    public ImageView videoState;
    public ProgressBar spinner;

    public RemoteVideoViewHolder(final View v) {
        super(v);
        this.videoState = (ImageView) v.findViewById(R.id.video_state);
        this.spinner = (ProgressBar) v.findViewById(R.id.spinner);
    }
}
