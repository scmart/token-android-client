package com.bakkenbaeck.token.network;


import com.bakkenbaeck.token.R;
import com.bakkenbaeck.token.model.adapter.BigDecimalAdapter;
import com.bakkenbaeck.token.model.adapter.BigIntegerAdapter;
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
        this.client = new OkHttpClient.Builder();

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
