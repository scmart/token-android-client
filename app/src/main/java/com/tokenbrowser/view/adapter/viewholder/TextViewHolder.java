package com.tokenbrowser.view.adapter.viewholder;

import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.tokenbrowser.model.local.SendState;
import com.tokenbrowser.token.R;
import com.tokenbrowser.view.BaseApplication;
import com.tokenbrowser.view.adapter.listeners.OnItemClickListener;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class TextViewHolder extends RecyclerView.ViewHolder {

    private View localContainer;
    private TextView localText;
    private TextView remoteText;
    private TextView sentStatusMessage;
    private ImageView localImage;
    private ImageView remoteImage;
    private LinearLayout localWrapper;
    private LinearLayout remoteWrapper;

    private String text;
    private boolean sentByLocal;
    private @SendState.State int sendState;
    private String attachmentFilename;

    public TextViewHolder(final View v) {
        super(v);
        this.localContainer = v.findViewById(R.id.local_container);
        this.localText = (TextView) v.findViewById(R.id.local_message);
        this.remoteText = (TextView) v.findViewById(R.id.remote_message);
        this.sentStatusMessage = (TextView) v.findViewById(R.id.sent_status_message);
        this.localImage = (ImageView) v.findViewById(R.id.local_image);
        this.remoteImage = (ImageView) v.findViewById(R.id.remote_image);
        this.localWrapper = (LinearLayout) v.findViewById(R.id.local_wrapper);
        this.remoteWrapper = (LinearLayout) v.findViewById(R.id.remote_wrapper);
    }

    public TextViewHolder setText(final String text) {
        this.text = text;
        return this;
    }

    public TextViewHolder setSentByLocal(final boolean sentByLocal) {
        this.sentByLocal = sentByLocal;
        return this;
    }

    public TextViewHolder setSendState(final @SendState.State int sendState) {
        this.sendState = sendState;
        return this;
    }

    public TextViewHolder setAttachmentFilename(final String fileName) {
        this.attachmentFilename = fileName;
        return this;
    }

    public TextViewHolder draw() {
        if (this.sentByLocal) {
            this.remoteWrapper.setVisibility(View.GONE);
            this.localContainer.setVisibility(View.VISIBLE);
            this.sentStatusMessage.setVisibility(View.GONE);
            this.localText.setText(text);
            loadImage(this.localImage);

            if (this.sendState == SendState.STATE_FAILED || this.sendState == SendState.STATE_PENDING) {
                this.sentStatusMessage.setVisibility(View.VISIBLE);
                this.sentStatusMessage.setText(this.sendState == SendState.STATE_FAILED
                        ? R.string.error__message_failed
                        : R.string.error__message_pending);
            }

            if (this.text == null) {
                this.localText.setVisibility(View.GONE);
            }

        } else {
            this.localContainer.setVisibility(View.GONE);
            this.remoteWrapper.setVisibility(View.VISIBLE);
            this.remoteText.setText(text);
            loadImage(this.remoteImage);

            if (this.text == null) {
                this.remoteText.setVisibility(View.GONE);
            }
        }

        return this;
    }

    private void loadImage(final ImageView imageView) {
        if (this.attachmentFilename != null) {
            imageView.setVisibility(View.VISIBLE);

            final String path = BaseApplication.get().getFilesDir() + "/" + this.attachmentFilename;
            final File imageFile = new File(path);

            Glide.with(this.itemView.getContext())
                    .load(imageFile)
                    .into(imageView);
        } else {
            imageView.setVisibility(View.GONE);
        }

        this.attachmentFilename = null;
    }

    public TextViewHolder setClickableImage(final OnItemClickListener<String> listener, final String filename) {
        this.localImage.setOnClickListener(v -> listener.onItemClick(filename));
        this.remoteImage.setOnClickListener(v -> listener.onItemClick(filename));
        return this;
    }

    public void setClickableUsernames(final OnItemClickListener<String> listener) {
        if (this.text == null) {
            return;
        }

        final SpannableString spannableString = new SpannableString(this.text);
        int lastEndPos = 0;

        for (final String word : getUsernames()) {
            final int currentStartPos = this.text.indexOf(word, lastEndPos);
            final int currentEndPos = this.text.indexOf(word, lastEndPos) + word.length();

            spannableString.setSpan(new ClickableSpan() {
                @Override
                public void onClick(final View view) {
                   handleSpannedClicked(view, listener, this);
                }
            }, currentStartPos,
               currentEndPos,
               Spanned.SPAN_INCLUSIVE_EXCLUSIVE);

            lastEndPos = currentEndPos;
        }

        this.localText.setText(spannableString);
        this.localText.setMovementMethod(LinkMovementMethod.getInstance());
        this.remoteText.setText(spannableString);
        this.remoteText.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private List<String> getUsernames() {
        final Pattern pattern = Pattern.compile("(?:^|\\s)@(\\w+)");
        final Matcher matcher = pattern.matcher(this.text);
        final List<String> matches = new ArrayList<>();

        while (matcher.find()) {
            matches.add(matcher.group(1));
        }

        return matches;
    }

    private void handleSpannedClicked(final View view,
                                      final OnItemClickListener<String> listener,
                                      final ClickableSpan clickableSpan) {
        final TextView tv = (TextView) view;
        final Spanned spannedString = (Spanned) tv.getText();
        final String username =
                spannedString
                        .subSequence(
                                spannedString.getSpanStart(clickableSpan),
                                spannedString.getSpanEnd(clickableSpan))
                        .toString();

        listener.onItemClick(username);
    }
}
