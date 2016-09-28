package com.bakkenbaeck.toshi.util;


import android.annotation.TargetApi;
import android.os.Build;

import com.bakkenbaeck.toshi.view.BaseApplication;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.text.DecimalFormat;
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
