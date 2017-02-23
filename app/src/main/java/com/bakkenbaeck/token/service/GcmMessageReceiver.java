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

import android.os.Bundle;

import com.bakkenbaeck.token.R;
import com.bakkenbaeck.token.crypto.signal.model.DecryptedSignalMessage;
import com.bakkenbaeck.token.crypto.util.TypeConverter;
import com.bakkenbaeck.token.model.local.PendingTransaction;
import com.bakkenbaeck.token.model.local.SofaMessage;
import com.bakkenbaeck.token.model.sofa.Payment;
import com.bakkenbaeck.token.model.sofa.SofaAdapters;
import com.bakkenbaeck.token.model.sofa.SofaType;
import com.bakkenbaeck.token.presenter.store.PendingTransactionStore;
import com.bakkenbaeck.token.util.EthUtil;
import com.bakkenbaeck.token.util.LogUtil;
import com.bakkenbaeck.token.view.BaseApplication;
import com.bakkenbaeck.token.view.notification.ChatNotificationManager;
import com.google.android.gms.gcm.GcmListenerService;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Locale;
import java.util.concurrent.TimeoutException;

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
        final DecryptedSignalMessage signalMessage;
        try {
            signalMessage = BaseApplication
                .get()
                .getTokenManager()
                .getSofaMessageManager()
                .fetchLatestMessage();
        } catch (final TimeoutException e) {
            LogUtil.i(getClass(), "Fetched all new messages");
            return;
        }

        ChatNotificationManager.showNotification(signalMessage);
        // There may be more messages.
        tryShowSignalMessage();

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

        new PendingTransactionStore()
                .load(payment.getTxHash())
                .subscribe((pt) -> handleTxLookup(pt, payment));
    }

    private void handleTxLookup(final PendingTransaction transaction, final Payment passedInPayment) {
        if (transaction == null) {
            LogUtil.w(getClass(), "Couldn't find pending transaction");
            renderNotificationForPayment(passedInPayment);
        } else {
            try {
                final Payment payment = adapters.paymentFrom(transaction.getSofaMessage().getPayload());
                renderNotificationForPayment(payment);
            } catch (IOException e) {
                // Shouldn't happen but we handle it anyway.
                renderNotificationForPayment(passedInPayment);
            }
        }
    }

    private void renderNotificationForPayment(final Payment payment) {
        final String title = this.getString(R.string.payment_received);
        final BigInteger weiAmount = TypeConverter.StringHexToBigInteger(payment.getValue());
        final BigDecimal ethAmount = EthUtil.weiToEth(weiAmount);
        final String localCurrency = BaseApplication.get().getTokenManager().getBalanceManager().convertEthToLocalCurrencyString(ethAmount);
        final String content = String.format(Locale.getDefault(), "Received: %s", localCurrency);
        ChatNotificationManager.showNotification(title, content, payment.getOwnerAddress());
    }
}
