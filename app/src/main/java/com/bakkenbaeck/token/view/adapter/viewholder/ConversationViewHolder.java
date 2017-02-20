package com.bakkenbaeck.token.view.adapter.viewholder;

import android.text.format.DateFormat;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bakkenbaeck.token.R;
import com.bakkenbaeck.token.model.local.Conversation;
import com.bakkenbaeck.token.model.local.User;
import com.bumptech.glide.Glide;

import java.util.Calendar;

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
        this.name.setText(member.getDisplayName());
        this.unreadCounter.setText(String.valueOf(conversation.getNumberOfUnread()));
        final String creationTime = getLastMessageCreationTime(conversation);
        this.time.setText(creationTime);

        final int visibility = conversation.getNumberOfUnread() > 0 ? VISIBLE : GONE;
        this.unreadCounter.setVisibility(visibility);

        Glide.with(this.avatar.getContext())
                .load(conversation.getMember().getAvatar())
                .into(this.avatar);
    }

    public void setLatestMessage(final String latestMessage) {
        this.latestMessage.setText(latestMessage);
    }

    private String getLastMessageCreationTime(final Conversation conversation) {
        final long creationTime = conversation.getLatestMessage().getCreationTime();
        final Calendar now = Calendar.getInstance();
        final Calendar lastMessageCreationTime = Calendar.getInstance();
        lastMessageCreationTime.setTimeInMillis(creationTime);

        if (now.get(Calendar.DAY_OF_YEAR) == lastMessageCreationTime.get(Calendar.DAY_OF_YEAR)) {
            return DateFormat.format("H:mm a", lastMessageCreationTime).toString();
        } else if (now.get(Calendar.WEEK_OF_YEAR) == lastMessageCreationTime.get(Calendar.WEEK_OF_YEAR)){
            return DateFormat.format("EEE", lastMessageCreationTime).toString();
        } else {
            return DateFormat.format("d MMM", lastMessageCreationTime).toString();
        }
    }
}