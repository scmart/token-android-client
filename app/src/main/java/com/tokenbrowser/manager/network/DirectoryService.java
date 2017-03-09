package com.tokenbrowser.manager.network;

import com.squareup.moshi.Moshi;
import com.tokenbrowser.manager.network.interceptor.LoggingInterceptor;
import com.tokenbrowser.manager.network.interceptor.OfflineCacheInterceptor;
import com.tokenbrowser.manager.network.interceptor.ReadFromCacheInterceptor;
import com.tokenbrowser.manager.network.interceptor.UserAgentInterceptor;
import com.tokenbrowser.token.R;
import com.tokenbrowser.view.BaseApplication;

import java.io.File;

import okhttp3.Cache;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.moshi.MoshiConverterFactory;
import rx.schedulers.Schedulers;

public class DirectoryService {

    private static DirectoryService instance;

    private final DirectoryInterface directoryInterface;
    private final OkHttpClient.Builder client;

    public static DirectoryInterface getApi() {
        return get().directoryInterface;
    }

    private static DirectoryService get() {
        if (instance == null) {
            instance = getSync();
        }
        return instance;
    }

    private static synchronized DirectoryService getSync() {
        if (instance == null) {
            instance = new DirectoryService();
        }
        return instance;
    }

    private DirectoryService() {
        final RxJavaCallAdapterFactory rxAdapter =
                RxJavaCallAdapterFactory.createWithScheduler(Schedulers.io());
        final File cachePath = new File(BaseApplication.get().getCacheDir(), "dirCache");
        this.client = new OkHttpClient
                .Builder()
                .cache(new Cache(cachePath, 1024 * 1024 * 5))
                .addNetworkInterceptor(new ReadFromCacheInterceptor())
                .addInterceptor(new OfflineCacheInterceptor());

        addUserAgentHeader();
        addLogging();

        final Moshi moshi = new Moshi.Builder()
                .build();

        final Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BaseApplication.get().getResources().getString(R.string.directory_url))
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .addCallAdapterFactory(rxAdapter)
                .client(client.build())
                .build();
        this.directoryInterface = retrofit.create(DirectoryInterface.class);
    }

    private void addUserAgentHeader() {
        this.client.addInterceptor(new UserAgentInterceptor());
    }

    private void addLogging() {
        final HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor(new LoggingInterceptor());
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        this.client.addInterceptor(interceptor);
    }
}
