package com.bakkenbaeck.token.view.adapter.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bakkenbaeck.token.R;

public final class RemoteVideoViewHolder extends RecyclerView.ViewHolder {
    public TextView title;
    public ImageView videoState;
    public TextView watched;

    public RemoteVideoViewHolder(final View v) {
        super(v);
        this.title = (TextView) v.findViewById(R.id.title);
        this.videoState = (ImageView) v.findViewById(R.id.video_state);
        this.watched = (TextView) v.findViewById(R.id.watched);
    }
}
