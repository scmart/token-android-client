package com.bakkenbaeck.token.manager.network.interceptor;


import com.bakkenbaeck.token.BuildConfig;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class UserAgentInterceptor implements Interceptor {

    private final String userAgent;

    public UserAgentInterceptor() {
        this.userAgent = "Android " + BuildConfig.APPLICATION_ID + " - " + BuildConfig.VERSION_NAME +  ":" + BuildConfig.VERSION_CODE;
    }
    @Override
    public Response intercept(final Chain chain) throws IOException {
        final Request original = chain.request();
        final Request request = original.newBuilder()
                .header("User-Agent", this.userAgent)
                .method(original.method(), original.body())
                .build();
        return chain.proceed(request);
    }
}
