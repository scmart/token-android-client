package com.bakkenbaeck.token.network.rest;


import com.bakkenbaeck.token.R;
import com.bakkenbaeck.token.model.jsonadapter.BigIntegerAdapter;
import com.bakkenbaeck.token.network.rest.interceptor.LoggingInterceptor;
import com.bakkenbaeck.token.network.rest.interceptor.UserAgentInterceptor;
import com.bakkenbaeck.token.view.BaseApplication;
import com.squareup.moshi.Moshi;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.moshi.MoshiConverterFactory;
import rx.schedulers.Schedulers;

public class BalanceService {

    private static BalanceService instance;

    private final BalanceInterface balanceInterface;
    private final OkHttpClient.Builder client;

    public static BalanceInterface getApi() {
        return get().balanceInterface;
    }

    private static BalanceService get() {
        if (instance == null) {
            instance = getSync();
        }
        return instance;
    }

    private static synchronized BalanceService getSync() {
        if (instance == null) {
            instance = new BalanceService();
        }
        return instance;
    }

    private BalanceService() {
        final RxJavaCallAdapterFactory rxAdapter = RxJavaCallAdapterFactory
                .createWithScheduler(Schedulers.io());
        this.client = new OkHttpClient.Builder();

        addUserAgentHeader();
        addLogging();

        final Moshi moshi = new Moshi.Builder()
                                    .add(new BigIntegerAdapter())
                                    .build();

        final Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BaseApplication.get().getResources().getString(R.string.balance_url))
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .addCallAdapterFactory(rxAdapter)
                .client(client.build())
                .build();
        this.balanceInterface = retrofit.create(BalanceInterface.class);
    }

    private void addUserAgentHeader() {
        this.client.addInterceptor(new UserAgentInterceptor());
    }

    private void addLogging() {
        final HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor(new LoggingInterceptor());
        this.client.addInterceptor(interceptor);
    }
}
