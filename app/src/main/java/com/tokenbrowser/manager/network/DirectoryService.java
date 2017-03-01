package com.tokenbrowser.manager.network;

import com.tokenbrowser.token.R;
import com.tokenbrowser.model.network.App;
import com.tokenbrowser.manager.network.interceptor.AppCacheInterceptor;
import com.tokenbrowser.manager.network.interceptor.LoggingInterceptor;
import com.tokenbrowser.manager.network.interceptor.UserAgentInterceptor;
import com.tokenbrowser.view.BaseApplication;
import com.squareup.moshi.Moshi;

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
    private final AppCacheInterceptor cacheInterceptor;

    public static DirectoryInterface getApi() {
        return get().directoryInterface;
    }

    public static App getCachedApp(final String appOwnerAddress) {
        return get().loadFromCache(appOwnerAddress);
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
        final RxJavaCallAdapterFactory rxAdapter = RxJavaCallAdapterFactory
                .createWithScheduler(Schedulers.io());
        this.cacheInterceptor = new AppCacheInterceptor();
        this.client = new OkHttpClient.Builder();
        this.client.interceptors().add(this.cacheInterceptor);

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
        this.client.addInterceptor(interceptor);
    }

    private App loadFromCache(final String appOwnerAddress) {
        return this.cacheInterceptor.loadFromCache(appOwnerAddress);
    }
}
