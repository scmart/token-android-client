package com.tokenbrowser.view.adapter.viewholder;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.tokenbrowser.model.local.SendState;
import com.tokenbrowser.R;
import com.tokenbrowser.view.adapter.listeners.OnItemClickListener;
import com.tokenbrowser.view.custom.RoundCornersImageView;

import java.io.File;

import de.hdodenhof.circleimageview.CircleImageView;

public final class ImageViewHolder extends RecyclerView.ViewHolder {

    private @Nullable CircleImageView avatar;
    private @Nullable TextView sentStatusMessage;
    private @NonNull RoundCornersImageView image;
    private @SendState.State int sendState;
    private String attachmentFilePath;
    private String avatarUri;

    public ImageViewHolder(final View v) {
        super(v);
        this.avatar = (CircleImageView) v.findViewById(R.id.avatar);
        this.sentStatusMessage = (TextView) v.findViewById(R.id.sent_status_message);
        this.image = (RoundCornersImageView) v.findViewById(R.id.image);
    }

    public ImageViewHolder setAvatarUri(final String uri) {
        this.avatarUri = uri;
        return this;
    }

    public ImageViewHolder setSendState(final @SendState.State int sendState) {
        this.sendState = sendState;
        return this;
    }

    public ImageViewHolder setAttachmentFilePath(final String filePath) {
        this.attachmentFilePath = filePath;
        return this;
    }

    public ImageViewHolder draw() {
        showImage();
        renderAvatar();
        setSendState();
        return this;
    }

    private void renderAvatar() {
        if (this.avatar == null) {
            return;
        }

        Glide
            .with(this.avatar.getContext())
            .load(this.avatarUri)
            .into(this.avatar);
    }

    private void showImage() {
        final File imageFile = new File(this.attachmentFilePath);
        this.image.setImage(imageFile);
        this.attachmentFilePath = null;
    }

    private void setSendState() {
        if (this.sentStatusMessage == null) {
            return;
        }

        this.sentStatusMessage.setVisibility(View.GONE);
        if (this.sendState == SendState.STATE_FAILED || this.sendState == SendState.STATE_PENDING) {
            this.sentStatusMessage.setVisibility(View.VISIBLE);
            this.sentStatusMessage.setText(this.sendState == SendState.STATE_FAILED
                    ? R.string.error__message_failed
                    : R.string.error__message_pending);
        }
    }

    public ImageViewHolder setClickableImage(final OnItemClickListener<String> listener, final String filePath) {
        this.image.setOnClickListener(v -> listener.onItemClick(filePath));
        return this;
    }
}
