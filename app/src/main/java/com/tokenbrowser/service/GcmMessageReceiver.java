/*
 * 	Copyright (c) 2017. Token Browser, Inc
 *
 * 	This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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

package com.tokenbrowser.service;

import android.os.Bundle;

import com.google.android.gms.gcm.GcmListenerService;
import com.tokenbrowser.R;
import com.tokenbrowser.crypto.HDWallet;
import com.tokenbrowser.crypto.signal.model.DecryptedSignalMessage;
import com.tokenbrowser.crypto.util.TypeConverter;
import com.tokenbrowser.manager.TokenManager;
import com.tokenbrowser.model.local.SofaMessage;
import com.tokenbrowser.model.sofa.Payment;
import com.tokenbrowser.model.sofa.SofaAdapters;
import com.tokenbrowser.model.sofa.SofaType;
import com.tokenbrowser.util.EthUtil;
import com.tokenbrowser.util.LogUtil;
import com.tokenbrowser.util.SharedPrefsUtil;
import com.tokenbrowser.view.BaseApplication;
import com.tokenbrowser.view.notification.ChatNotificationManager;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Locale;
import java.util.concurrent.TimeoutException;

import rx.Single;

public class GcmMessageReceiver extends GcmListenerService {

    private final SofaAdapters adapters;

    public GcmMessageReceiver() {
        this.adapters = new SofaAdapters();
    }

    @Override
    public void onMessageReceived(final String from, final Bundle data) {
        if (SharedPrefsUtil.hasSignedOut()) return;

        tryInitApp()
        .subscribe(
                __ -> handleIncomingMessage(data),
                this::handleIncomingMessageError
        );
    }

    private Single<TokenManager> tryInitApp() {
        return BaseApplication
                .get()
                .getTokenManager()
                .tryInit();
    }

    private void handleIncomingMessage(final Bundle data) {
        try {
            final String messageBody = data.getString("message");
            LogUtil.i(getClass(), "Incoming PN: " + messageBody);

            if (messageBody == null) {
                tryShowSignalMessage();
                return;
            }

            final SofaMessage sofaMessage = new SofaMessage().makeNew(messageBody);

            if (sofaMessage.getType() == SofaType.PAYMENT) {
                final Payment payment = this.adapters.paymentFrom(sofaMessage.getPayload());
                handlePayment(payment);
            } else {
                tryShowSignalMessage();
            }

        } catch (final Exception ex) {
            LogUtil.e(getClass(), "Error -> " + ex);
        }
    }

    private void handleIncomingMessageError(final Throwable throwable) {
        LogUtil.e(getClass(), "Error during incoming message " + throwable.toString());
    }

    private void handlePayment(final Payment payment) {
        payment.getPaymentDirection()
               .subscribe(
                   (paymentDirection) -> this.handleValidPayment(payment, paymentDirection),
                   this::handleInvalidPayment
               );
    }

    private void handleValidPayment(final Payment payment, final @Payment.PaymentDirection Integer paymentDirection) {
        switch (paymentDirection) {
            case Payment.FROM_LOCAL_USER:
                updatePayment(payment);
                refreshBalance();
                return;
            case Payment.TO_LOCAL_USER:
                updatePayment(payment);
                refreshBalance();
                showPaymentNotification(payment);
                return;
            default:
            case Payment.NOT_RELEVANT:
                handleInvalidPayment(new IllegalArgumentException("Not handling transaction that doesn't involve local user"));
        }
    }

    private void handleInvalidPayment(final Throwable throwable) {
        LogUtil.e(getClass(), "Invalid payment " + throwable.toString());
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

    private void updatePayment(final Payment payment) {
        BaseApplication
                .get()
                .getTokenManager()
                .getTransactionManager()
                .updatePayment(payment);
    }

    private void refreshBalance() {
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

        BaseApplication
                .get()
                .getTokenManager()
                .getWallet()
                .subscribe((w) -> showOnlyIncomingPaymentNotification(w, payment));
    }

    private void showOnlyIncomingPaymentNotification(final HDWallet wallet, final Payment payment) {
        if (wallet.getPaymentAddress().equals(payment.getFromAddress())) {
            // This payment was sent by us. Show no notification.
            LogUtil.i(getClass(), "Suppressing payment notification. Payment sent by local user.");
            return;
        }
        renderNotificationForPayment(payment);
    }

    private void renderNotificationForPayment(final Payment payment) {
        final BigInteger weiAmount = TypeConverter.StringHexToBigInteger(payment.getValue());
        final BigDecimal ethAmount = EthUtil.weiToEth(weiAmount);
        BaseApplication
                .get()
                .getTokenManager()
                .getBalanceManager()
                .convertEthToLocalCurrencyString(ethAmount)
                .subscribe((localCurrency) -> {
                    final String content = String.format(Locale.getDefault(), this.getString(R.string.latest_message__payment_incoming), localCurrency);
                    BaseApplication
                            .get()
                            .getTokenManager()
                            .getUserManager()
                            .getUserFromPaymentAddress(payment.getFromAddress())
                            .subscribe((sender) -> ChatNotificationManager.showChatNotification(sender, content));
                });
    }
}
