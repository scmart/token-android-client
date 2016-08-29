package com.bakkenbaeck.toshi.http;


import com.bakkenbaeck.toshi.model.jsonadapter.BigIntegerAdapter;
import com.bakkenbaeck.toshi.util.LogUtil;
import com.squareup.moshi.Moshi;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.moshi.MoshiConverterFactory;
import rx.schedulers.Schedulers;

public class ToshiService {

    private static final String BASE_URL = "https://toshi-app.herokuapp.com";
    private static ToshiService instance;

    private final ToshiInterface toshiInterface;
    private final OkHttpClient.Builder client;

    public static ToshiInterface getApi() {
        return get().toshiInterface;
    }

    private static ToshiService get() {
        if (instance == null) {
            instance = getSync();
        }
        return instance;
    }

    private static synchronized ToshiService getSync() {
        if (instance == null) {
            instance = new ToshiService();
        }
        return instance;
    }

    private ToshiService() {
        final RxJavaCallAdapterFactory rxAdapter = RxJavaCallAdapterFactory.createWithScheduler(Schedulers.io());
        this.client = new OkHttpClient.Builder();

        addLogging();

        final Moshi moshi = new Moshi.Builder()
                                    .add(new BigIntegerAdapter())
                                    .build();

        final Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .addCallAdapterFactory(rxAdapter)
                .client(client.build())
                .build();
        this.toshiInterface = retrofit.create(ToshiInterface.class);
    }

    private void addLogging() {
        final HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {
            @Override
            public void log(final String message) {
                LogUtil.print(getClass(), message);
            }
        });
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        this.client.addInterceptor(interceptor);
    }
}
