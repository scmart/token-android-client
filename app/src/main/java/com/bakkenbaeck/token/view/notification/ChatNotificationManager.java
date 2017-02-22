package com.bakkenbaeck.token.view.notification;


import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;

import com.bakkenbaeck.token.R;
import com.bakkenbaeck.token.crypto.signal.model.DecryptedSignalMessage;
import com.bakkenbaeck.token.model.local.SofaMessage;
import com.bakkenbaeck.token.model.local.User;
import com.bakkenbaeck.token.model.sofa.SofaAdapters;
import com.bakkenbaeck.token.model.sofa.SofaType;
import com.bakkenbaeck.token.util.OnNextSubscriber;
import com.bakkenbaeck.token.view.BaseApplication;
import com.bakkenbaeck.token.view.activity.ChatActivity;

import java.io.IOException;

public class ChatNotificationManager {

    private static String currentlyOpenConversation;

    public static void suppressNotificationsForConversation(final String conversationId) {
        currentlyOpenConversation = conversationId;
    }

    public static void stopNotificationSuppresion() {
        currentlyOpenConversation = null;
    }

    public static void showNotification(final DecryptedSignalMessage signalMessage) {
        if (signalMessage == null) {
            return;
        }

        BaseApplication
            .get()
            .getTokenManager()
            .getUserManager()
            .getUserFromAddress(signalMessage.getSource())
            .subscribe(new OnNextSubscriber<User>() {
                @Override
                public void onNext(final User user) {
                    if (user == null) {
                        return;
                    }
                    unsubscribe();
                    handleUserLookup(user, signalMessage);
                }
            });
    }

    private static void handleUserLookup(final User user, final DecryptedSignalMessage signalMessage) {
        final String body = getBodyFromMessage(signalMessage);
        showNotification(user.getDisplayName(), body, user.getOwnerAddress());
    }

    private static String getBodyFromMessage(final DecryptedSignalMessage dsm) {
        final SofaMessage sofaMessage = new SofaMessage().makeNew(dsm.getBody());
        try {
            if (sofaMessage.getType() == SofaType.PLAIN_TEXT) {
                return new SofaAdapters().messageFrom(sofaMessage.getPayload()).getBody();
            }
        } catch (final IOException ex) {
            // Nop
        }
        return sofaMessage.getPayload();
    }

    public static void showNotification(final String title, final String content, final String ownerAddress) {
        if (ownerAddress.equals(currentlyOpenConversation)) {
            return;
        }

        final Uri notificationSound = Uri.parse("android.resource://" + BaseApplication.get().getPackageName() + "/" + R.raw.notification);
        final NotificationCompat.Builder builder = new NotificationCompat.Builder(BaseApplication.get())
                .setSmallIcon(R.drawable.token)
                .setContentTitle(title)
                .setContentText(content)
                .setAutoCancel(true)
                .setColor(ContextCompat.getColor(BaseApplication.get(), R.color.colorPrimary))
                .setSound(notificationSound)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        final NotificationCompat.BigTextStyle bigTextBuilder = new NotificationCompat.BigTextStyle(builder);
        final PendingIntent pendingIntent = generateChatIntentWithBackStack(ownerAddress);
        builder.setContentIntent(pendingIntent);

        final NotificationManager manager = (NotificationManager) BaseApplication.get().getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(ownerAddress, 1, bigTextBuilder.build());
    }

    private static PendingIntent generateChatIntentWithBackStack(final String ownerAddress) {
        final Intent chatIntent = new Intent(BaseApplication.get(), ChatActivity.class);
        chatIntent.putExtra(ChatActivity.EXTRA__REMOTE_USER_ADDRESS, ownerAddress);
        return TaskStackBuilder.create(BaseApplication.get())
                .addParentStack(ChatActivity.class)
                .addNextIntent(chatIntent)
                .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
    }
}
