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

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.content.LocalBroadcastManager;

import com.bakkenbaeck.token.R;
import com.bakkenbaeck.token.crypto.HDWallet;
import com.bakkenbaeck.token.model.network.Addresses;
import com.bakkenbaeck.token.model.network.GcmRegistration;
import com.bakkenbaeck.token.model.network.ServerTime;
import com.bakkenbaeck.token.network.BalanceService;
import com.bakkenbaeck.token.util.LogUtil;
import com.bakkenbaeck.token.view.BaseApplication;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import java.util.ArrayList;
import java.util.List;

import rx.SingleSubscriber;
import rx.schedulers.Schedulers;

public class RegistrationIntentService extends IntentService {

    public static final String SENT_TOKEN_TO_SERVER = "sentTokenToServer";
    public static final String REGISTRATION_COMPLETE = "registrationComplete";
    public static final String WATCHING_TRANSACTIONS = "watchingTransactions";

    private final SharedPreferences sharedPreferences;

    public RegistrationIntentService() {
        super("RegIntentService");
        this.sharedPreferences = BaseApplication.get().getSharedPreferences(BaseApplication.get().getResources().getString(R.string.gcm_pref_filename), Context.MODE_PRIVATE);
    }

    @Override
    protected void onHandleIntent(final Intent intent) {
        try {
            final InstanceID instanceID = InstanceID.getInstance(this);
            final String token = instanceID.getToken(getString(R.string.gcm_defaultSenderId),
                    GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
            LogUtil.i(getClass(), "GCM Registration Token: " + token);
            registerGcmToken(token);
        } catch (final Exception ex) {
            LogUtil.d(getClass(), "Failed to complete token refresh" + ex);
            sharedPreferences.edit().putBoolean(SENT_TOKEN_TO_SERVER, false).apply();
        }

        final Intent registrationComplete = new Intent(REGISTRATION_COMPLETE);
        LocalBroadcastManager.getInstance(this).sendBroadcast(registrationComplete);
    }

    private void registerGcmToken(final String token) {
        BalanceService
                .getApi()
                .getTimestamp()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(new SingleSubscriber<ServerTime>() {
                    @Override
                    public void onSuccess(final ServerTime serverTime) {
                        registerGcmTokenWithTimestamp(token, serverTime.get());
                    }

                    @Override
                    public void onError(final Throwable error) {
                        sharedPreferences.edit().putBoolean(SENT_TOKEN_TO_SERVER, false).apply();
                        LogUtil.e(getClass(), "sendRegistrationToServer onError " + error);
                    }
                });
    }

    private void registerGcmTokenWithTimestamp(final String token, final long timestamp) {
        BalanceService
                .getApi()
                .registerGcm(timestamp, new GcmRegistration(token))
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(new SingleSubscriber<Void>() {
                    @Override
                    public void onSuccess(final Void value) {
                        sharedPreferences.edit().putBoolean(SENT_TOKEN_TO_SERVER, true).apply();
                        getWalletForTransactionListening(timestamp);
                    }

                    @Override
                    public void onError(final Throwable error) {
                        sharedPreferences.edit().putBoolean(SENT_TOKEN_TO_SERVER, false).apply();
                        LogUtil.e(getClass(), "regGcm onError " + error);
                    }
                });
    }

    private void getWalletForTransactionListening(final long timestamp) {
        BaseApplication.get()
                .getTokenManager()
                .getWallet()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(new SingleSubscriber<HDWallet>() {
                    @Override
                    public void onSuccess(final HDWallet wallet) {
                        registerForTransactionsOnWallet(timestamp, wallet);
                    }

                    @Override
                    public void onError(final Throwable error) {
                        LogUtil.e(getClass(), "getWallet onError " + error);
                    }
                });
    }

    private void registerForTransactionsOnWallet(final long timestamp, final HDWallet wallet) {
        final List<String> list = new ArrayList<>();
        list.add(wallet.getWalletAddress());

        final Addresses addresses = new Addresses(list);

        BalanceService
                .getApi()
                .startWatchingAddresses(timestamp, addresses)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(new SingleSubscriber<Void>() {
                    @Override
                    public void onSuccess(final Void value) {
                        sharedPreferences.edit().putBoolean(SENT_TOKEN_TO_SERVER, true).apply();
                    }

                    @Override
                    public void onError(final Throwable error) {
                        LogUtil.e(getClass(), "registerAddress onError" + error);
                    }
                });
    }
}
