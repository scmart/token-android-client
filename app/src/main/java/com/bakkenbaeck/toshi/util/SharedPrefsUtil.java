package com.bakkenbaeck.toshi.util;

import android.content.SharedPreferences;

import com.bakkenbaeck.toshi.view.BaseApplication;
import com.securepreferences.SecurePreferences;

public class SharedPrefsUtil {
    public static final String IS_VERIFIED = "SharedPrefsUtil";

    public static boolean isVerified(){
        SharedPreferences prefs = new SecurePreferences(BaseApplication.get(), "", "um");
        return prefs.getBoolean(IS_VERIFIED, false);
    }

    public static void saveVerified(boolean b){
        SharedPreferences prefs = new SecurePreferences(BaseApplication.get(), "", "um");
        prefs.edit().putBoolean(IS_VERIFIED, b).commit();
    }
}
