package com.tokenbrowser.manager.network.image;

import android.content.Context;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader;
import com.bumptech.glide.load.model.ModelLoaderFactory;
import com.bumptech.glide.module.GlideModule;
import com.tokenbrowser.manager.network.interceptor.LoggingInterceptor;
import com.tokenbrowser.manager.network.interceptor.OfflineCacheInterceptor;
import com.tokenbrowser.manager.network.interceptor.ReadFromCacheInterceptor;
import com.tokenbrowser.manager.network.interceptor.UserAgentInterceptor;
import com.tokenbrowser.view.BaseApplication;

import java.io.File;
import java.io.InputStream;

import okhttp3.Cache;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

public class GlideOkHttpStack implements GlideModule {

    private static final int MAX_SIZE = 1024 * 1024 * 10;

    @Override
    public void applyOptions(Context context, GlideBuilder builder) {}

    @Override
    public void registerComponents(Context context, Glide glide) {
        final File cacheDir = new File(BaseApplication.get().getCacheDir(), "TokenImageCache");
        final Cache cache = new Cache(cacheDir, MAX_SIZE);

        final OkHttpClient client =
                new OkHttpClient().newBuilder()
                .cache(cache)
                .addNetworkInterceptor(new ReadFromCacheInterceptor())
                .addInterceptor(new UserAgentInterceptor())
                .addInterceptor(new OfflineCacheInterceptor())
                .addInterceptor(new HttpLoggingInterceptor(new LoggingInterceptor()).setLevel(HttpLoggingInterceptor.Level.BODY))
                .build();


        glide.register(CachedGlideUrl.class, InputStream.class, superFactory(new OkHttpUrlLoader.Factory(client), CachedGlideUrl.class));
        glide.register(ForceLoadGlideUrl.class, InputStream.class, superFactory(new OkHttpUrlLoader.Factory(client), ForceLoadGlideUrl.class));
    }

    /**
     * {@link Glide#register(Class, Class, ModelLoaderFactory) Register}'s params should be
     * {@code (Class<T>, Class<Y>, ModelLoaderFactory<? super T, Y>)}. This method works around that.
     */
    @SuppressWarnings({"unchecked", "unused"})
    private static <T> ModelLoaderFactory<T, InputStream> superFactory(
            ModelLoaderFactory<? super T, InputStream> factory, Class<T> modelType) {
        return (ModelLoaderFactory<T, InputStream>)factory;
    }
}