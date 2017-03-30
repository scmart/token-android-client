package com.tokenbrowser.manager.network;

import android.content.Context;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.module.GlideModule;
import com.tokenbrowser.manager.network.interceptor.LoggingInterceptor;
import com.tokenbrowser.view.BaseApplication;

import java.io.File;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.CacheControl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;

import static com.google.common.net.HttpHeaders.CACHE_CONTROL;

public class TokenGlideModule implements GlideModule {

    private static final int MAX_SIZE = 1024 * 1024 * 10;

    @Override
    public void applyOptions(Context context, GlideBuilder builder) {}

    @Override
    public void registerComponents(Context context, Glide glide) {
        final OkHttpClient.Builder client = new OkHttpClient().newBuilder();
        final File cacheDir = new File(BaseApplication.get().getCacheDir(), "TokenImageCache");
        final Cache cache = new Cache(cacheDir, MAX_SIZE);
        client.cache(cache);

        client.addNetworkInterceptor(chain -> {
            final Request req = chain.request();
            final CacheControl cacheControl = new CacheControl.Builder()
                    .maxAge(1, TimeUnit.DAYS)
                    .build();
            return chain.proceed(req.newBuilder()
                    .header(CACHE_CONTROL, cacheControl.toString())
                    .build());
        });

        client.addInterceptor(new HttpLoggingInterceptor(new LoggingInterceptor())
                .setLevel(HttpLoggingInterceptor.Level.BODY));

        glide.register(GlideUrl.class, InputStream.class, new OkHttpUrlLoader.Factory(client.build()));
    }
}