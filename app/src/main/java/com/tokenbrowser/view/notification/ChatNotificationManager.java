package com.tokenbrowser.view.notification;


import android.app.NotificationManager;
import android.content.Context;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;

import com.tokenbrowser.crypto.signal.model.DecryptedSignalMessage;
import com.tokenbrowser.model.local.SofaMessage;
import com.tokenbrowser.model.local.User;
import com.tokenbrowser.model.sofa.SofaAdapters;
import com.tokenbrowser.model.sofa.SofaType;
import com.tokenbrowser.R;
import com.tokenbrowser.util.LogUtil;
import com.tokenbrowser.view.BaseApplication;
import com.tokenbrowser.view.notification.model.ChatNotification;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatNotificationManager {

    private static String currentlyOpenConversation;
    private static final Map<String, ChatNotification> activeNotifications = new HashMap<>();

    public static void suppressNotificationsForConversation(final String conversationId) {
        currentlyOpenConversation = conversationId;
        final NotificationManager manager = (NotificationManager) BaseApplication.get().getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(conversationId, 1);
        handleNotificationDismissed(conversationId);
    }

    public static void handleNotificationDismissed(final String notificationTag) {
        activeNotifications.remove(notificationTag);
    }

    public static void stopNotificationSuppression() {
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
            .subscribe((user) -> handleUserLookup(user, signalMessage));
    }

    private static void handleUserLookup(final User user, final DecryptedSignalMessage signalMessage) {
        final String body = getBodyFromMessage(signalMessage);
        if (body == null) {
            // This wasn't a SOFA::Message. Do not render.
            LogUtil.i(ChatNotificationManager.class, "Not rendering PN");
            return;
        }
        showChatNotification(user, body);
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

    public static void showChatNotification(
            final User sender,
            final String content) {

        // Sender will be null if the transaction came from outside of the Token platform.
        final String notificationKey = sender == null ? ChatNotification.DEFAULT_TAG : sender.getTokenId();

        if (notificationKey.equals(currentlyOpenConversation)) {
            return;
        }

        final ChatNotification activeChatNotification
                = activeNotifications.get(notificationKey) != null
                ? activeNotifications.get(notificationKey)
                : new ChatNotification(sender);

        activeNotifications.put(notificationKey, activeChatNotification);

        activeChatNotification
                .addUnreadMessage(content)
                .generateLargeIcon()
                .subscribe(() -> showChatNotification(activeChatNotification));
    }

    private static void showChatNotification(final ChatNotification chatNotification) {
        final NotificationCompat.Style style = generateNotificationStyle(chatNotification);
        final CharSequence contextText = chatNotification.getLastMessage();


        final int lightOnRate = 1000 * 2;
        final int lightOffRate = 1000 * 15;
        final int notificationColor = ContextCompat.getColor(BaseApplication.get(), R.color.colorPrimary);
        final Uri notificationSound = Uri.parse("android.resource://" + BaseApplication.get().getPackageName() + "/" + R.raw.notification);

        final NotificationCompat.Builder builder = new NotificationCompat.Builder(BaseApplication.get())
                .setSmallIcon(R.drawable.ic_notification)
                .setLargeIcon(chatNotification.getLargeIcon())
                .setContentTitle(chatNotification.getTitle())
                .setContentText(contextText)
                .setTicker(contextText)
                .setAutoCancel(true)
                .setColor(notificationColor)
                .setSound(notificationSound)
                .setLights(notificationColor, lightOnRate, lightOffRate)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setStyle(style)
                .setNumber(chatNotification.getNumberOfUnreadMessages())
                .setContentIntent(chatNotification.getPendingIntent())
                .setDeleteIntent(chatNotification.getDeleteIntent());

        final int maxNumberMessagesWithSound = 3;
        if (chatNotification.getNumberOfUnreadMessages() > maxNumberMessagesWithSound) {
            builder
                .setSound(null)
                .setVibrate(null);
        }

        final NotificationManager manager = (NotificationManager) BaseApplication.get().getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(chatNotification.getTag(), 1, builder.build());
    }

    private static NotificationCompat.Style generateNotificationStyle(final ChatNotification chatNotification) {
        final int numberOfUnreadMessages = chatNotification.getNumberOfUnreadMessages();

        if (numberOfUnreadMessages == 1) {
            return new NotificationCompat
                    .BigTextStyle()
                    .setBigContentTitle(chatNotification.getTitle())
                    .bigText(chatNotification.getLastMessage());
        }

        final List<String> lastFewMessages = chatNotification.getLastFewMessages();
        final NotificationCompat.Style style =
                new NotificationCompat
                        .InboxStyle()
                        .setBigContentTitle(chatNotification.getTitle());
        for (final String message : lastFewMessages) {
            ((NotificationCompat.InboxStyle) style).addLine(message);
        }
        return style;
    }
}
