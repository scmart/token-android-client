package com.bakkenbaeck.token.network.rest;


import com.bakkenbaeck.token.BuildConfig;
import com.bakkenbaeck.token.model.jsonadapter.BigIntegerAdapter;
import com.bakkenbaeck.token.util.LogUtil;
import com.squareup.moshi.Moshi;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.moshi.MoshiConverterFactory;
import rx.schedulers.Schedulers;

public class TokenService {

    private static final String BASE_URL = "https://toshi-app.herokuapp.com";
    private static TokenService instance;

    private final TokenInterface tokenInterface;
    private final OkHttpClient.Builder client;

    public static TokenInterface getApi() {
        return get().tokenInterface;
    }

    private static TokenService get() {
        if (instance == null) {
            instance = getSync();
        }
        return instance;
    }

    private static synchronized TokenService getSync() {
        if (instance == null) {
            instance = new TokenService();
        }
        return instance;
    }

    private TokenService() {
        final RxJavaCallAdapterFactory rxAdapter = RxJavaCallAdapterFactory.createWithScheduler(Schedulers.io());
        this.client = new OkHttpClient.Builder();

        addUserAgentHeader();
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
        this.tokenInterface = retrofit.create(TokenInterface.class);
    }

    private void addUserAgentHeader() {
        final Interceptor userAgentInterceptor = new Interceptor() {
            @Override
            public Response intercept(Interceptor.Chain chain) throws IOException {
                final Request original = chain.request();
                final Request request = original.newBuilder()
                        .header("User-Agent", String.valueOf(BuildConfig.VERSION_CODE))
                        .method(original.method(), original.body())
                        .build();
                return chain.proceed(request);
            }
        };
        this.client.addInterceptor(userAgentInterceptor);
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
