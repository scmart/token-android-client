package com.tokenbrowser.service;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.tokenbrowser.view.notification.ChatNotificationManager;

public class NotificationDismissedReceiver extends BroadcastReceiver {

    public static final String TAG = "notification_tag";

    @Override
    public void onReceive(final Context context, final Intent intent) {
        final String notificationTag = intent.getExtras().getString(TAG);
        ChatNotificationManager.handleNotificationDismissed(notificationTag);
    }
}
