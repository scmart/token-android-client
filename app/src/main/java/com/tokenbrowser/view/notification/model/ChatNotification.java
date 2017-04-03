package com.tokenbrowser.view.notification.model;


import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.TaskStackBuilder;

import com.bumptech.glide.Glide;
import com.tokenbrowser.model.local.User;
import com.tokenbrowser.service.NotificationDismissedReceiver;
import com.tokenbrowser.R;
import com.tokenbrowser.view.BaseApplication;
import com.tokenbrowser.view.activity.ChatActivity;
import com.tokenbrowser.view.activity.MainActivity;
import com.tokenbrowser.view.custom.CropCircleTransformation;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import rx.Completable;

public class ChatNotification {

    public static final String DEFAULT_TAG = "unknown";

    private final User sender;
    private final ArrayList<String> messages;
    private static final int MAXIMUM_NUMBER_OF_SHOWN_MESSAGES = 5;
    private Bitmap largeIcon;

    public ChatNotification(final User sender) {
        this.sender = sender;
        this.messages = new ArrayList<>();
    }

    public ChatNotification addUnreadMessage(final String unreadMessage) {
        this.messages.add(unreadMessage);
        return this;
    }

    public String getTag() {
        return this.sender == null ? DEFAULT_TAG : sender.getTokenId();
    }

    public String getTitle() {
        return this.sender == null
                ? BaseApplication.get().getString(R.string.unknown_sender)
                : this.sender.getDisplayName();
    }

    public Bitmap getLargeIcon() {
        return this.largeIcon;
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
                .getPendingIntent(getTitle().hashCode(), PendingIntent.FLAG_ONE_SHOT);
    }

    public PendingIntent getDeleteIntent() {
        final Intent intent =
                new Intent(BaseApplication.get(), NotificationDismissedReceiver.class)
                        .putExtra(NotificationDismissedReceiver.TAG, getTag());

        return PendingIntent.getBroadcast(
                BaseApplication.get(),
                1,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public int getNumberOfUnreadMessages() {
        return messages.size();
    }

    public CharSequence getLastMessage() {
        return messages.size() == 0
                ? ""
                : messages.get(messages.size() -1);
    }

    public Completable generateLargeIcon() {
        if (this.largeIcon != null) return Completable.complete();
        if (getAvatarUri() == null) return Completable.fromAction(this::setDefaultLargeIcon);

        return Completable.fromAction(() -> {
            try {
                fetchUserAvatar();
            } catch (InterruptedException | ExecutionException e) {
                setDefaultLargeIcon();
            }
        });
    }

    private void fetchUserAvatar() throws InterruptedException, ExecutionException {
        this.largeIcon = Glide
                        .with(BaseApplication.get())
                        .load(getAvatarUri())
                        .asBitmap()
                        .transform(new CropCircleTransformation(BaseApplication.get()))
                        .into(200, 200)
                        .get();
    }

    private Bitmap setDefaultLargeIcon() {
        return this.largeIcon = BitmapFactory.decodeResource(BaseApplication.get().getResources(), R.mipmap.ic_launcher);
    }

    private String getAvatarUri() {
        return this.sender == null
                ? null
                : this.sender.getAvatar();
    }
}
