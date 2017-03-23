package com.tokenbrowser.view.adapter.viewholder;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.tokenbrowser.model.local.SendState;
import com.tokenbrowser.token.R;
import com.tokenbrowser.view.adapter.listeners.OnItemClickListener;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.hdodenhof.circleimageview.CircleImageView;

public final class TextViewHolder extends RecyclerView.ViewHolder {

    private @NonNull TextView message;
    private @Nullable CircleImageView avatar;
    private @Nullable TextView sentStatusMessage;

    private String text;
    private @SendState.State int sendState;
    private String avatarUri;

    public TextViewHolder(final View v) {
        super(v);
        this.message = (TextView) v.findViewById(R.id.message);
        this.avatar = (CircleImageView) v.findViewById(R.id.avatar);
        this.sentStatusMessage = (TextView) v.findViewById(R.id.sent_status_message);
    }

    public TextViewHolder setText(final String text) {
        this.text = text;
        return this;
    }

    public TextViewHolder setAvatarUri(final String uri) {
        this.avatarUri = uri;
        return this;
    }

    public TextViewHolder setSendState(final @SendState.State int sendState) {
        this.sendState = sendState;
        return this;
    }

    public TextViewHolder draw() {
        showText();
        renderAvatar();
        setSendState();
        return this;
    }

    private void showText() {
        this.message.setText(this.text);
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

        this.message.setText(spannableString);
        this.message.setMovementMethod(LinkMovementMethod.getInstance());
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
