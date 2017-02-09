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

import com.bakkenbaeck.token.model.local.ChatMessage;
import com.bakkenbaeck.token.model.sofa.Payment;
import com.bakkenbaeck.token.model.sofa.SofaAdapters;
import com.bakkenbaeck.token.model.sofa.SofaType;
import com.bakkenbaeck.token.util.LogUtil;
import com.bakkenbaeck.token.view.BaseApplication;
import com.google.android.gms.gcm.GcmListenerService;

import java.io.IOException;

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
            final ChatMessage chatMessage = new ChatMessage().makeNew(message);

            if (chatMessage.getType() == SofaType.PAYMENT) {
                final Payment payment = adapters.paymentFrom(chatMessage.getPayload());
                handleIncomingPayment(payment);
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
}
