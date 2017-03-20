package com.tokenbrowser.view.notification.model;


import android.app.PendingIntent;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.app.TaskStackBuilder;

import com.tokenbrowser.model.local.User;
import com.tokenbrowser.token.R;
import com.tokenbrowser.view.BaseApplication;
import com.tokenbrowser.view.activity.ChatActivity;
import com.tokenbrowser.view.activity.MainActivity;

import java.util.ArrayList;
import java.util.List;

public class ChatNotification {

    private final User sender;
    private final ArrayList<String> messages;
    private static final int MAXIMUM_NUMBER_OF_SHOWN_MESSAGES = 5;

    public ChatNotification(final User sender) {
        this.sender = sender;
        this.messages = new ArrayList<>();
    }

    public void addUnreadMessage(final String unreadMessage) {
        this.messages.add(unreadMessage);
    }

    @Nullable
    public User getSender() {
        return this.sender;
    }

    public String getTitle() {
        return this.sender == null
                ? BaseApplication.get().getString(R.string.unknown_sender)
                : this.sender.getDisplayName();
    }

    public List<String> getLastFewMessages() {
        if (messages.size() == 0) {
            return new ArrayList<>(0);
        }

        final int end = Math.max(messages.size(), 0);
        final int start = Math.max(end - MAXIMUM_NUMBER_OF_SHOWN_MESSAGES, 0);
        return messages.subList(start, end);
    }

    public PendingIntent getPendingIntent() {
        final Intent mainIntent = new Intent(BaseApplication.get(), MainActivity.class);
        mainIntent.putExtra(MainActivity.EXTRA__ACTIVE_TAB, 1);

        if (this.sender == null || this.sender.getTokenId() == null) {
            return TaskStackBuilder.create(BaseApplication.get())
                    .addParentStack(MainActivity.class)
                    .addNextIntent(mainIntent)
                    .getPendingIntent(0, PendingIntent.FLAG_ONE_SHOT);
        }

        final Intent chatIntent = new Intent(BaseApplication.get(), ChatActivity.class);
        chatIntent.putExtra(ChatActivity.EXTRA__REMOTE_USER_ADDRESS, this.sender.getTokenId());

        return TaskStackBuilder.create(BaseApplication.get())
                .addParentStack(MainActivity.class)
                .addNextIntent(mainIntent)
                .addNextIntent(chatIntent)
                .getPendingIntent(0, PendingIntent.FLAG_ONE_SHOT);
    }

    public int getNumberOfUnreadMessages() {
        return messages.size();
    }

    public CharSequence getLastMessage() {
        return messages.size() == 0
                ? ""
                : messages.get(messages.size() -1);
    }
}
