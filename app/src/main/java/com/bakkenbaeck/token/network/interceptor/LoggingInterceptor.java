package com.bakkenbaeck.token.network.interceptor;

import com.bakkenbaeck.token.util.LogUtil;

import okhttp3.logging.HttpLoggingInterceptor;


public class LoggingInterceptor implements HttpLoggingInterceptor.Logger {
    @Override
    public void log(final String message) {
        LogUtil.print(getClass(), message);
    }
}
