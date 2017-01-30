/**
 * Copyright 2015 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bakkenbaeck.token.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.bakkenbaeck.token.R;
import com.bakkenbaeck.token.model.local.ChatMessage;
import com.bakkenbaeck.token.model.sofa.Payment;
import com.bakkenbaeck.token.model.sofa.SofaAdapters;
import com.bakkenbaeck.token.util.LogUtil;
import com.bakkenbaeck.token.view.activity.MainActivity;
import com.google.android.gms.gcm.GcmListenerService;

import java.io.IOException;

public class GcmMessageReceiver extends GcmListenerService {

    @Override
    public void onMessageReceived(final String from, final Bundle data) {
        LogUtil.d(getClass(), "onMessageReceived");
        final String message = data.getString("message");

        try {
            final SofaAdapters adapters = new SofaAdapters();
            final ChatMessage chatMessage = new ChatMessage().setPayload(message);
            final Payment payment = adapters.paymentFrom(chatMessage.getPayload());

            sendNotification(from, payment.getLocalPrice());
        } catch (IOException e) {
            LogUtil.e(getClass(), "Error -> " + e);
        }
    }

    private void sendNotification(final String from, final String amount) {
        final Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        final PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT);

        final Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.token)
                .setContentTitle(getString(R.string.payment_received, from))
                .setContentText(getString(R.string.payment_amount, amount))
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        final NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0, notificationBuilder.build());
    }
}
