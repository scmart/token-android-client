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

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.content.LocalBroadcastManager;

import com.tokenbrowser.token.R;
import com.tokenbrowser.util.FileNames;
import com.tokenbrowser.util.LogUtil;
import com.tokenbrowser.view.BaseApplication;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import rx.schedulers.Schedulers;

public class RegistrationIntentService extends IntentService {

    public static final String FORCE_UPDATE = "update_token";
    public static final String CHAT_SERVICE_SENT_TOKEN_TO_SERVER = "chatServiceSentTokenToServer";
    public static final String ETH_SERVICE_SENT_TOKEN_TO_SERVER = "sentTokenToServer";
    public static final String REGISTRATION_COMPLETE = "registrationComplete";
    public static final String WATCHING_TRANSACTIONS = "watchingTransactions";

    private final SharedPreferences sharedPreferences;

    public RegistrationIntentService() {
        super("RegIntentService");
        this.sharedPreferences = BaseApplication.get().getSharedPreferences(FileNames.GCM_PREFS, Context.MODE_PRIVATE);
    }

    @Override
    protected void onHandleIntent(final Intent intent) {
        try {
            final InstanceID instanceID = InstanceID.getInstance(this);
            final String token = instanceID.getToken(getString(R.string.gcm_defaultSenderId),
                    GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
            LogUtil.i(getClass(), "GCM Registration Token: " + token);

            final boolean forceUpdate = intent.getBooleanExtra(FORCE_UPDATE, false);
            registerEthereumServiceGcmToken(token, forceUpdate);
            registerChatServiceGcm(token, forceUpdate);
        } catch (final Exception ex) {
            LogUtil.d(getClass(), "Failed to complete token refresh" + ex);
            sharedPreferences.edit().putBoolean(ETH_SERVICE_SENT_TOKEN_TO_SERVER, false).apply();
        }

        final Intent registrationComplete = new Intent(REGISTRATION_COMPLETE);
        LocalBroadcastManager.getInstance(this).sendBroadcast(registrationComplete);
    }

    private void registerChatServiceGcm(final String token, final boolean forceUpdate) {
        final boolean sentToServer = sharedPreferences.getBoolean(CHAT_SERVICE_SENT_TOKEN_TO_SERVER, false);
        final boolean watchingTransactions = sharedPreferences.getBoolean(WATCHING_TRANSACTIONS, false);

        if (!forceUpdate && sentToServer) {
            if (!watchingTransactions) {
                watchWalletTransactions();
            }
        }

        BaseApplication
                .get()
                .getTokenManager()
                .getSofaMessageManager()
                .setGcmToken(token);
    }

    private void registerEthereumServiceGcmToken(final String token, final boolean forceUpdate) {
        final boolean sentToServer = sharedPreferences.getBoolean(ETH_SERVICE_SENT_TOKEN_TO_SERVER, false);
        if (!forceUpdate && sentToServer) {
            return;
        }

        BaseApplication
                .get()
                .getTokenManager()
                .getBalanceManager()
                .registerForGcm(token, forceUpdate)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(this::handleGcmSuccess, this::handleGcmFailure);
    }

    public void handleGcmSuccess(final Void unused) {
        this.sharedPreferences.edit().putBoolean(ETH_SERVICE_SENT_TOKEN_TO_SERVER, true).apply();
        watchWalletTransactions();
    }

    private void watchWalletTransactions() {
        BaseApplication
                .get()
                .getTokenManager()
                .getBalanceManager()
                .watchForWalletTransactions()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(this::handleWatchWalletSuccess, this::handleWatchWalletFailure);
    }

    public void handleGcmFailure(final Throwable error) {
        this.sharedPreferences.edit().putBoolean(ETH_SERVICE_SENT_TOKEN_TO_SERVER, false).apply();
        LogUtil.e(getClass(), "regGcm onError " + error);
    }

    private void handleWatchWalletSuccess(final Void unused) {
        sharedPreferences.edit().putBoolean(WATCHING_TRANSACTIONS, true).apply();
    }

    private void handleWatchWalletFailure(final Throwable error) {
        sharedPreferences.edit().putBoolean(WATCHING_TRANSACTIONS, false).apply();
        LogUtil.e(getClass(), "registerAddress onError " + error);
    }
}
