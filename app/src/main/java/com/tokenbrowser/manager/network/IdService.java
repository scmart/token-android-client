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
import com.tokenbrowser.manager.network.interceptor.SigningInterceptor;
import com.tokenbrowser.manager.network.interceptor.UserAgentInterceptor;
import com.tokenbrowser.model.adapter.RealmListAdapter;
import com.tokenbrowser.R;
import com.tokenbrowser.view.BaseApplication;

import java.io.File;
import java.io.IOException;

import okhttp3.Cache;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.moshi.MoshiConverterFactory;
import rx.schedulers.Schedulers;

public class IdService {

    private static IdService instance;

    private final IdInterface idInterface;
    private final OkHttpClient.Builder client;
    private final Cache cache;

    public static IdInterface getApi() {
        return get().idInterface;
    }

    public static IdService get() {
        if (instance == null) {
            instance = getSync();
        }
        return instance;
    }

    private static synchronized IdService getSync() {
        if (instance == null) {
            instance = new IdService();
        }
        return instance;
    }

    private IdService() {
        final RxJavaCallAdapterFactory rxAdapter = RxJavaCallAdapterFactory.createWithScheduler(Schedulers.io());
        final File cachePath = new File(BaseApplication.get().getCacheDir(), "idCache");
        this.cache = new Cache(cachePath, 1024 * 1024 * 2);
        this.client = new OkHttpClient
                .Builder()
                .cache(this.cache)
                .addNetworkInterceptor(new ReadFromCacheInterceptor())
                .addInterceptor(new OfflineCacheInterceptor());

        addUserAgentHeader();
        addSigningInterceptor();
        addLogging();

        final Moshi moshi = new Moshi.Builder()
                .add(new RealmListAdapter())
                .build();

        final Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BaseApplication.get().getResources().getString(R.string.id_url))
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .addCallAdapterFactory(rxAdapter)
                .client(client.build())
                .build();
        this.idInterface = retrofit.create(IdInterface.class);
    }

    private void addUserAgentHeader() {
        this.client.addInterceptor(new UserAgentInterceptor());
    }

    private void addSigningInterceptor() {
        this.client.addInterceptor(new SigningInterceptor());
    }

    private void addLogging() {
        final HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor(new LoggingInterceptor());
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        this.client.addInterceptor(interceptor);
    }

    public void clearCache() throws IOException {
        this.cache.evictAll();
    }
}
