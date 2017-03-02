package com.tokenbrowser.view.notification;


import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;

import com.tokenbrowser.token.R;
import com.tokenbrowser.crypto.signal.model.DecryptedSignalMessage;
import com.tokenbrowser.model.local.SofaMessage;
import com.tokenbrowser.model.local.User;
import com.tokenbrowser.model.sofa.SofaAdapters;
import com.tokenbrowser.model.sofa.SofaType;
import com.tokenbrowser.util.LogUtil;
import com.tokenbrowser.util.OnNextSubscriber;
import com.tokenbrowser.view.BaseApplication;
import com.tokenbrowser.view.activity.ChatActivity;
import com.tokenbrowser.view.activity.MainActivity;

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
        if (body == null) {
            // This wasn't a SOFA::Message. Do not render.
            LogUtil.i(ChatNotificationManager.class, "Not rendering PN");
            return;
        }
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
        return null;
    }

    public static void showNotification(final String title, final String content, final String ownerAddress) {
        if (ownerAddress != null && ownerAddress.equals(currentlyOpenConversation)) {
            return;
        }

        final Uri notificationSound = Uri.parse("android.resource://" + BaseApplication.get().getPackageName() + "/" + R.raw.notification);
        final NotificationCompat.Builder builder = new NotificationCompat.Builder(BaseApplication.get())
                .setSmallIcon(R.drawable.ic_notification)
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
        final String tag = ownerAddress == null ? "unknown" : ownerAddress;
        manager.notify(tag, 1, bigTextBuilder.build());
    }

    private static PendingIntent generateChatIntentWithBackStack(final String ownerAddress) {
        final Intent mainIntent = new Intent(BaseApplication.get(), MainActivity.class);
        mainIntent.putExtra(MainActivity.EXTRA__ACTIVE_TAB, 1);
        final Intent chatIntent = new Intent(BaseApplication.get(), ChatActivity.class);
        chatIntent.putExtra(ChatActivity.EXTRA__REMOTE_USER_ADDRESS, ownerAddress);

        if (ownerAddress == null) {
            return TaskStackBuilder.create(BaseApplication.get())
                    .addParentStack(MainActivity.class)
                    .addNextIntent(mainIntent)
                    .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        }

        return TaskStackBuilder.create(BaseApplication.get())
                .addParentStack(MainActivity.class)
                .addNextIntent(mainIntent)
                .addNextIntent(chatIntent)
                .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
    }
}
