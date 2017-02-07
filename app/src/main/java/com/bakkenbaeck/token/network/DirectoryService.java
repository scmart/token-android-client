package com.bakkenbaeck.token.network;

import com.bakkenbaeck.token.R;
import com.bakkenbaeck.token.network.interceptor.LoggingInterceptor;
import com.bakkenbaeck.token.network.interceptor.UserAgentInterceptor;
import com.bakkenbaeck.token.view.BaseApplication;
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
        final RxJavaCallAdapterFactory rxAdapter = RxJavaCallAdapterFactory
                .createWithScheduler(Schedulers.io());
        this.client = new OkHttpClient.Builder();

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
}
