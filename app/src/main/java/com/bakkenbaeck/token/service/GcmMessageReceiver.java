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
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;

import com.bakkenbaeck.token.R;
import com.bakkenbaeck.token.crypto.signal.model.DecryptedSignalMessage;
import com.bakkenbaeck.token.crypto.util.TypeConverter;
import com.bakkenbaeck.token.model.local.SofaMessage;
import com.bakkenbaeck.token.model.local.User;
import com.bakkenbaeck.token.model.sofa.Payment;
import com.bakkenbaeck.token.model.sofa.SofaAdapters;
import com.bakkenbaeck.token.model.sofa.SofaType;
import com.bakkenbaeck.token.util.EthUtil;
import com.bakkenbaeck.token.util.LogUtil;
import com.bakkenbaeck.token.util.OnNextSubscriber;
import com.bakkenbaeck.token.view.BaseApplication;
import com.bakkenbaeck.token.view.activity.ChatActivity;
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
            final String messageBody = data.getString("message");
            LogUtil.i(getClass(), "Incoming PN: " + messageBody);

            if (messageBody == null) {
                tryShowSignalMessage();
                return;
            }

            final SofaMessage sofaMessage = new SofaMessage().makeNew(messageBody);

            if (sofaMessage.getType() == SofaType.PAYMENT) {
                final Payment payment = adapters.paymentFrom(sofaMessage.getPayload());
                handleIncomingPayment(payment);
                showPaymentNotification(payment);
            } else {
                tryShowSignalMessage();
            }

        } catch (final Exception ex) {
            LogUtil.e(getClass(), "Error -> " + ex);
        }
    }

    private void tryShowSignalMessage() {
        final DecryptedSignalMessage signalMessage =
            BaseApplication
                .get()
                .getTokenManager()
                .getSofaMessageManager()
                .fetchLatestMessage();
        if (signalMessage == null) {
            LogUtil.i(getClass(), "Incoming PN; but no idea what it was!");
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

    private void handleUserLookup(final User user, final DecryptedSignalMessage signalMessage) {
        final String body = getBodyFromMessage(signalMessage);
        showNotification(user.getDisplayName(), body, user.getOwnerAddress());
        // There may be more messages.
        tryShowSignalMessage();
    }

    private String getBodyFromMessage(final DecryptedSignalMessage dsm) {
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
        showNotification(title, content, payment.getOwnerAddress());
    }

    private void showNotification(final String title, final String content, final String ownerAddress) {
        final Uri notificationSound = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.notification);
        final NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.token)
                .setContentTitle(title)
                .setContentText(content)
                .setAutoCancel(true)
                .setColor(ContextCompat.getColor(this, R.color.colorPrimary))
                .setSound(notificationSound)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        final NotificationCompat.BigTextStyle bigTextBuilder = new NotificationCompat.BigTextStyle(builder);
        final PendingIntent pendingIntent = generateChatIntentWithBackStack(ownerAddress);
        builder.setContentIntent(pendingIntent);

        final NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(ownerAddress.hashCode(), bigTextBuilder.build());
    }

    private PendingIntent generateChatIntentWithBackStack(final String ownerAddress) {
        final Intent chatIntent = new Intent(this, ChatActivity.class);
        chatIntent.putExtra(ChatActivity.EXTRA__REMOTE_USER_ADDRESS, ownerAddress);
        return TaskStackBuilder.create(this)
                .addParentStack(ChatActivity.class)
                .addNextIntent(chatIntent)
                .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
    }
}
