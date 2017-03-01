package com.tokenbrowser.manager.network.interceptor;

import com.tokenbrowser.util.LogUtil;

import okhttp3.logging.HttpLoggingInterceptor;


public class LoggingInterceptor implements HttpLoggingInterceptor.Logger {
    @Override
    public void log(final String message) {
        LogUtil.print(getClass(), message);
    }
}
