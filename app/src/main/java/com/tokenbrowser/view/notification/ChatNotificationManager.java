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
import com.tokenbrowser.token.R;
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
        activeNotifications.remove(conversationId);
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
        if (sender.getTokenId() != null && sender.getTokenId().equals(currentlyOpenConversation)) {
            return;
        }

        ChatNotification activeChatNotification = activeNotifications.get(sender.getTokenId());
        if (activeChatNotification == null) {
            activeChatNotification = new ChatNotification(sender);
            activeNotifications.put(sender.getTokenId(), activeChatNotification);
        }

        activeChatNotification.addUnreadMessage(content);
        showChatNotification(activeChatNotification);
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
                .setContentIntent(chatNotification.getPendingIntent());

        final int maxNumberMessagesWithSound = 3;
        if (chatNotification.getNumberOfUnreadMessages() > maxNumberMessagesWithSound) {
            builder
                .setSound(null)
                .setVibrate(null);
        }

        final NotificationManager manager = (NotificationManager) BaseApplication.get().getSystemService(Context.NOTIFICATION_SERVICE);
        final String tag = chatNotification.getSender().getTokenId();
        manager.notify(tag, 1, builder.build());
    }

    private static NotificationCompat.Style generateNotificationStyle(final ChatNotification chatNotification) {
        final int numberOfUnreadMessages = chatNotification.getNumberOfUnreadMessages();

        if (numberOfUnreadMessages == 1) {
            return new NotificationCompat
                    .BigTextStyle()
                    .setBigContentTitle(chatNotification.getSender().getDisplayName())
                    .bigText(chatNotification.getLastMessage());
        }

        final List<String> lastFewMessages = chatNotification.getLastFewMessages();
        final NotificationCompat.Style style =
                new NotificationCompat
                        .InboxStyle()
                        .setBigContentTitle(chatNotification.getSender().getDisplayName());
        for (final String message : lastFewMessages) {
            ((NotificationCompat.InboxStyle) style).addLine(message);
        }
        return style;
    }
}
