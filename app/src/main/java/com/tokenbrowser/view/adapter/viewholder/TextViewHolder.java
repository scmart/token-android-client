package com.tokenbrowser.view.adapter.viewholder;

import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.TextView;

import com.tokenbrowser.model.local.SendState;
import com.tokenbrowser.token.R;
import com.tokenbrowser.view.adapter.listeners.OnItemClickListener;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class TextViewHolder extends RecyclerView.ViewHolder {

    private View localContainer;
    private TextView localText;
    private TextView remoteText;
    private TextView sentFailedMessage;

    private String text;
    private boolean sentByLocal;
    private @SendState.State int sendState;

    public TextViewHolder(final View v) {
        super(v);
        this.localContainer = v.findViewById(R.id.local_container);
        this.localText = (TextView) v.findViewById(R.id.local_message);
        this.remoteText = (TextView) v.findViewById(R.id.remote_message);
        this.sentFailedMessage = (TextView) v.findViewById(R.id.sent_status_message);
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

    public TextViewHolder draw() {
        if (this.sentByLocal) {
            this.remoteText.setVisibility(View.GONE);
            this.localContainer.setVisibility(View.VISIBLE);
            this.sentFailedMessage.setVisibility(View.GONE);
            this.localText.setText(text);

            if (this.sendState == SendState.STATE_FAILED) {
                this.sentFailedMessage.setVisibility(View.VISIBLE);
            }
        } else {
            this.localContainer.setVisibility(View.GONE);
            this.remoteText.setVisibility(View.VISIBLE);
            this.remoteText.setText(text);
        }

        return this;
    }

    public void setClickableItems(final OnItemClickListener<String> listener) {
        final SpannableString spannableString = new SpannableString(this.text);
        for (final String word : getUsernames()){
            spannableString.setSpan(new ClickableSpan() {
                @Override
                public void onClick(final View view) {
                   handleSpannedClicked(view, listener, this);
                }
            }, this.text.indexOf(word),
               this.text.indexOf(word) + word.length(),
               Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        this.localText.setText(spannableString);
        this.localText.setMovementMethod(LinkMovementMethod.getInstance());
        this.remoteText.setText(spannableString);
        this.remoteText.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private List<String> getUsernames() {
        final Pattern pattern = Pattern.compile("@\\w+");
        final Matcher matcher = pattern.matcher(this.text);
        final List<String> matches = new ArrayList<>();
        while (matcher.find()) {
            matches.add(matcher.group());
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
                        .subSequence(spannedString.getSpanStart(clickableSpan), spannedString.getSpanEnd(clickableSpan))
                        .toString()
                        .substring(1);
        listener.onItemClick(username);
    }
}
