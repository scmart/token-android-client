package com.bakkenbaeck.token.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import com.bakkenbaeck.token.R;
import com.bakkenbaeck.token.view.BaseApplication;

public class SharedPrefsUtil {
    private static final String STORED_QR_CODE = "STORED_QR_CODE";

    public static byte[] getQrCode(){
        final SharedPreferences prefs = BaseApplication.get().getSharedPreferences(BaseApplication.get().getResources().getString(R.string.user_manager_pref_filename), Context.MODE_PRIVATE);
        final String byteString = prefs.getString(STORED_QR_CODE, null);

        if(byteString == null){
            return null;
        }

        return Base64.decode(byteString, Base64.DEFAULT);
    }

    public static void saveQrCode(final byte[] array){
        final SharedPreferences prefs = BaseApplication.get().getSharedPreferences(BaseApplication.get().getResources().getString(R.string.user_manager_pref_filename), Context.MODE_PRIVATE);
        final String byteString = Base64.encodeToString(array, Base64.DEFAULT);
        prefs.edit().putString(STORED_QR_CODE, byteString).apply();
    }
}
