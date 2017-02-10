package com.bakkenbaeck.token.view.adapter.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.bakkenbaeck.token.R;
import com.bakkenbaeck.token.model.local.SendState;

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

    public void draw() {
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
    }
}
