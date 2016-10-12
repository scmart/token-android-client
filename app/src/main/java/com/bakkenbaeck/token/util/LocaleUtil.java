package com.bakkenbaeck.token.util;


import android.annotation.TargetApi;
import android.os.Build;

import com.bakkenbaeck.token.view.BaseApplication;

import java.util.Locale;

public class LocaleUtil {

    @TargetApi(Build.VERSION_CODES.N)
    public static Locale getLocale() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            return BaseApplication.get().getResources().getConfiguration().getLocales().get(0);
        } else {
            return BaseApplication.get().getResources().getConfiguration().locale;
        }
    }
}
