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

package com.tokenbrowser.manager.network;


import com.squareup.moshi.Moshi;
import com.tokenbrowser.manager.network.interceptor.LoggingInterceptor;
import com.tokenbrowser.manager.network.interceptor.OfflineCacheInterceptor;
import com.tokenbrowser.manager.network.interceptor.ReadFromCacheInterceptor;
import com.tokenbrowser.manager.network.interceptor.UserAgentInterceptor;
import com.tokenbrowser.model.adapter.BigDecimalAdapter;
import com.tokenbrowser.model.adapter.BigIntegerAdapter;
import com.tokenbrowser.R;
import com.tokenbrowser.view.BaseApplication;

import java.io.File;

import okhttp3.Cache;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.moshi.MoshiConverterFactory;
import rx.schedulers.Schedulers;

public class CurrencyService {

    private static CurrencyService instance;

    private final CurrencyInterface currencyInterface;
    private final OkHttpClient.Builder client;

    public static CurrencyInterface getApi() {
        return get().currencyInterface;
    }

    private static CurrencyService get() {
        if (instance == null) {
            instance = getSync();
        }
        return instance;
    }

    private static synchronized CurrencyService getSync() {
        if (instance == null) {
            instance = new CurrencyService();
        }
        return instance;
    }

    private CurrencyService() {
        final RxJavaCallAdapterFactory rxAdapter = RxJavaCallAdapterFactory
                .createWithScheduler(Schedulers.io());
        final File cachePath = new File(BaseApplication.get().getCacheDir(), "ratesCache");
        this.client = new OkHttpClient
                .Builder()
                .cache(new Cache(cachePath, 1024 * 1024))
                .addNetworkInterceptor(new ReadFromCacheInterceptor())
                .addInterceptor(new OfflineCacheInterceptor());

        addUserAgentHeader();
        addLogging();

        final Moshi moshi = new Moshi.Builder()
                .add(new BigIntegerAdapter())
                .add(new BigDecimalAdapter())
                .build();

        final Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BaseApplication.get().getResources().getString(R.string.currency_url))
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .addCallAdapterFactory(rxAdapter)
                .client(client.build())
                .build();
        this.currencyInterface = retrofit.create(CurrencyInterface.class);
    }

    private void addUserAgentHeader() {
        this.client.addInterceptor(new UserAgentInterceptor());
    }

    private void addLogging() {
        final HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor(new LoggingInterceptor());
        this.client.addInterceptor(interceptor);
    }
}
