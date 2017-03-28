package com.tokenbrowser.util;


import android.util.Log;

import com.tokenbrowser.BuildConfig;

public class LogUtil  {

    private LogUtil() {}

    // More verbose wrapper functions

    @SuppressWarnings("rawtypes")
    public static void print(final Class callingClass, final String message) {
        i(callingClass, message);
    }

    @SuppressWarnings("rawtypes")
    public static void error(final Class callingClass, final String message) {
        e(callingClass, message);
    }

    // Wrappers that allow to be called with a class

    @SuppressWarnings("rawtypes")
    public static void d(final Class callingClass, final String message) {
        final String logTag = getLogTag(callingClass);
        d(logTag, message);
    }

    @SuppressWarnings("rawtypes")
    public static void e(final Class callingClass, final String message) {
        final String logTag = getLogTag(callingClass);
        e(logTag, message);
    }

    @SuppressWarnings("rawtypes")
    public static void i(final Class callingClass, final String message) {
        final String logTag = getLogTag(callingClass);
        i(logTag, message);
    }

    @SuppressWarnings("rawtypes")
    public static void v(final Class callingClass, final String message) {
        final String logTag = getLogTag(callingClass);
        v(logTag, message);
    }

    @SuppressWarnings("rawtypes")
    public static void w(final Class callingClass, final String message) {
        final String logTag = getLogTag(callingClass);
        w(logTag, message);
    }

    @SuppressWarnings("rawtypes")
    public static void wtf(final Class callingClass, final String message) {
        final String logTag = getLogTag(callingClass);
        wtf(logTag, message);
    }

    // Wrappers that protect against log output at runtime

    public static void d(final String tag, final String message) {
        if (BuildConfig.DEBUG) {
            Log.d(tag, message);
        }
    }

    public static void e(final String tag, final String message) {
        if (BuildConfig.DEBUG) {
            Log.e(tag, message);
        }
    }

    public static void i(final String tag, final String message) {
        if (BuildConfig.DEBUG) {
            Log.i(tag, message);
        }
    }

    public static void v(final String tag, final String message) {
        if (BuildConfig.DEBUG) {
            Log.v(tag, message);
        }
    }

    public static void w(final String tag, final String message) {
        if (BuildConfig.DEBUG) {
            Log.w(tag, message);
        }
    }

    public static void wtf(final String tag, final String message) {
        if (BuildConfig.DEBUG) {
            Log.wtf(tag, message);
        }
    }

    @SuppressWarnings("rawtypes")
    private static String getLogTag(final Class clz) {
        final String logPrefix = BuildConfig.APPLICATION_ID;
        if (clz.isAnonymousClass()) {
            return logPrefix + clz.getEnclosingClass().getSimpleName();
        }
        return logPrefix + clz.getSimpleName();
    }
}
