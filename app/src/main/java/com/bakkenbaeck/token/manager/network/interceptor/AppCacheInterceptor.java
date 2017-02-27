package com.bakkenbaeck.token.manager.network.interceptor;


import com.bakkenbaeck.token.model.network.App;
import com.bakkenbaeck.token.model.network.Apps;
import com.bakkenbaeck.token.util.LogUtil;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import okhttp3.Interceptor;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class AppCacheInterceptor implements Interceptor {

    private final JsonAdapter<Apps> adapter;
    private final HashMap<String, App> cache;

    public AppCacheInterceptor() {
        final Moshi moshi = new Moshi.Builder().build();
        this.adapter = moshi.adapter(Apps.class);
        this.cache = new HashMap<>();
    }

    public App loadFromCache(final String appOwnerAddress) {
        return cache.get(appOwnerAddress);
    }

    @Override
    public Response intercept(final Chain chain) throws IOException {
        final Response response = chain.proceed(chain.request());
        final ResponseBody responseBody = response.body();
        final String appsJson = responseBody.string();

        try {
            final Apps apps = this.adapter.fromJson(appsJson);
            addToCache(apps.getApps());
        } catch (final IOException ex) {
            LogUtil.i(getClass(), "Error deserialising reponse. Not caching result.");
        }

        return response.newBuilder().body(ResponseBody.create(responseBody.contentType(), appsJson.getBytes())).build();
    }

    private void addToCache(final List<App> apps) {
        for (final App app : apps) {
            this.cache.put(app.getOwnerAddress(), app);
        }
    }
}
