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
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.bakkenbaeck.token.R;
import com.bakkenbaeck.token.crypto.util.TypeConverter;
import com.bakkenbaeck.token.model.local.SofaMessage;
import com.bakkenbaeck.token.model.sofa.Payment;
import com.bakkenbaeck.token.model.sofa.SofaAdapters;
import com.bakkenbaeck.token.model.sofa.SofaType;
import com.bakkenbaeck.token.util.EthUtil;
import com.bakkenbaeck.token.util.LogUtil;
import com.bakkenbaeck.token.view.BaseApplication;
import com.bakkenbaeck.token.view.activity.MainActivity;
import com.google.android.gms.gcm.GcmListenerService;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Locale;

public class GcmMessageReceiver extends GcmListenerService {

    private final SofaAdapters adapters;

    public GcmMessageReceiver() {
        this.adapters = new SofaAdapters();
    }

    @Override
    public void onMessageReceived(final String from, final Bundle data) {
        try {
            final String message = data.getString("message");
            LogUtil.i(getClass(), "Incoming PN: " + message);

            if (message == null) {
                final String title = this.getString(R.string.message_received);
                showNotification(title, null);
                return;
            }

            final SofaMessage sofaMessage = new SofaMessage().makeNew(message);

            if (sofaMessage.getType() == SofaType.PAYMENT) {
                final Payment payment = adapters.paymentFrom(sofaMessage.getPayload());
                handleIncomingPayment(payment);
                showPaymentNotification(payment);
            } else {
                final String title = this.getString(R.string.message_received);
                //Message is atm null, use sofaMessage instead
                showNotification(title, message);
            }

        } catch (IOException e) {
            LogUtil.e(getClass(), "Error -> " + e);
        }
    }

    private void handleIncomingPayment(final Payment payment) {
        BaseApplication
                .get()
                .getTokenManager()
                .getTransactionManager()
                .updatePayment(payment);

        BaseApplication
                .get()
                .getTokenManager()
                .getBalanceManager()
                .refreshBalance();
    }

    private void showPaymentNotification(final Payment payment) {
        if (payment.getStatus().equals(SofaType.CONFIRMED)) {
            return;
        }

        final String title = this.getString(R.string.payment_received);
        final BigInteger weiAmount = TypeConverter.StringHexToBigInteger(payment.getValue());
        final BigDecimal ethAmount = EthUtil.weiToEth(weiAmount);
        final String localCurrency = BaseApplication.get().getTokenManager().getBalanceManager().convertEthToLocalCurrencyString(ethAmount);
        final String content = String.format(Locale.getDefault(), "Received: %s", localCurrency);
        showNotification(title, content);
    }

    private void showNotification(final String title, final String content) {
        final Uri notificationSound = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.notification);
        final NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.token)
                .setContentTitle(title)
                .setContentText(content)
                .setAutoCancel(true)
                .setSound(notificationSound)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        NotificationCompat.BigTextStyle bigTextBuilder = new NotificationCompat.BigTextStyle(builder);

        final Intent notificationIntent = new Intent(this, MainActivity.class);
        final PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(contentIntent);

        final NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(0, bigTextBuilder.build());
    }
}
