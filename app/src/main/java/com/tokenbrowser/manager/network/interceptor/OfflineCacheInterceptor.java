package com.tokenbrowser.manager.network.interceptor;


import com.tokenbrowser.view.BaseApplication;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.CacheControl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

import static com.google.common.net.HttpHeaders.CACHE_CONTROL;

public class OfflineCacheInterceptor implements Interceptor {

    @Override
    public Response intercept(final Chain chain) throws IOException, IllegalStateException {
        Request request = chain.request();

        CacheControl cacheControl = new CacheControl.Builder()
                .maxAge(6, TimeUnit.SECONDS)
                .maxStale(14, TimeUnit.DAYS)
                .build();

        if (!BaseApplication.get().isConnected()) {
            cacheControl = new CacheControl.Builder()
                    .maxAge(14, TimeUnit.DAYS)
                    .maxStale(14, TimeUnit.DAYS)
                    .build();
        }

        request = request.newBuilder()
                .header(CACHE_CONTROL, cacheControl.toString())
                .build();

        return chain.proceed(request);
    }
}
