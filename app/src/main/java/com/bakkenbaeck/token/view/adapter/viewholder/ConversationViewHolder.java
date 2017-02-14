package com.bakkenbaeck.token.view.adapter.viewholder;


import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bakkenbaeck.token.R;
import com.bakkenbaeck.token.model.local.Conversation;
import com.bakkenbaeck.token.model.local.User;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class ConversationViewHolder extends ClickableViewHolder {

    private ImageView avatar;
    private TextView name;
    private TextView latestMessage;
    private TextView time;
    private TextView unreadCounter;

    public ConversationViewHolder(final View view) {
        super(view);
        this.name = (TextView) view.findViewById(R.id.name);
        this.avatar = (ImageView) view.findViewById(R.id.avatar);
        this.latestMessage = (TextView) view.findViewById(R.id.latest_message);
        this.time = (TextView) view.findViewById(R.id.time);
        this.unreadCounter = (TextView) view.findViewById(R.id.unread_counter);
    }

    public void setConversation(final Conversation conversation) {
        final User member = conversation.getMember();
        this.name.setText(member.getUsername());
        this.unreadCounter.setText(String.valueOf(conversation.getNumberOfUnread()));

        final int visibility = conversation.getNumberOfUnread() > 0 ? VISIBLE : GONE;
        this.unreadCounter.setVisibility(visibility);
    }

    public void setLatestMessage(final String latestMessage) {
        this.latestMessage.setText(latestMessage);
    }
}