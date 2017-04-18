/*
 * 	Copyright (c) 2017. Token Browser, Inc
 *
 * 	This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.tokenbrowser.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import com.tokenbrowser.view.BaseApplication;

public class SharedPrefsUtil {
    private static final String STORED_QR_CODE = "STORED_QR_CODE";
    private static final String HAS_ONBOARDED = "hasOnboarded";
    private static final String HAS_SIGNED_OUT = "hasSignedIn";
    private static final String HAS_BACKED_UP_PHRASE = "hasBackedUpPhrase";
    private static final String HAS_LOADED_APP_FIRST_TIME = "hasLoadedApp";

    public static boolean hasOnboarded() {
        final SharedPreferences prefs = BaseApplication.get().getSharedPreferences(FileNames.USER_PREFS, Context.MODE_PRIVATE);
        return prefs.getBoolean(HAS_ONBOARDED, false);
    }

    public static void setHasOnboarded() {
        final SharedPreferences prefs = BaseApplication.get().getSharedPreferences(FileNames.USER_PREFS, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(HAS_ONBOARDED, true).apply();
    }

    public static boolean hasLoadedApp() {
        final SharedPreferences prefs = BaseApplication.get().getSharedPreferences(FileNames.USER_PREFS, Context.MODE_PRIVATE);
        return prefs.getBoolean(HAS_LOADED_APP_FIRST_TIME, false);
    }

    public static void setHasLoadedApp() {
        final SharedPreferences prefs = BaseApplication.get().getSharedPreferences(FileNames.USER_PREFS, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(HAS_LOADED_APP_FIRST_TIME, true).apply();
    }

    public static byte[] getQrCode() {
        final SharedPreferences prefs = BaseApplication.get().getSharedPreferences(FileNames.USER_PREFS, Context.MODE_PRIVATE);
        final String byteString = prefs.getString(STORED_QR_CODE, null);

        if (byteString == null) {
            return null;
        }

        return Base64.decode(byteString, Base64.DEFAULT);
    }

    public static void saveQrCode(final byte[] array) {
        final SharedPreferences prefs = BaseApplication.get().getSharedPreferences(FileNames.USER_PREFS, Context.MODE_PRIVATE);
        final String byteString = Base64.encodeToString(array, Base64.DEFAULT);
        prefs.edit().putString(STORED_QR_CODE, byteString).apply();
    }

    public static void setSignedIn() {
        final SharedPreferences prefs = BaseApplication.get().getSharedPreferences(FileNames.USER_PREFS, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(HAS_SIGNED_OUT, false).apply();
    }

    public static void setSignedOut() {
        final SharedPreferences prefs = BaseApplication.get().getSharedPreferences(FileNames.USER_PREFS, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(HAS_SIGNED_OUT, true).apply();
    }

    public static boolean hasSignedOut() {
        final SharedPreferences prefs = BaseApplication.get().getSharedPreferences(FileNames.USER_PREFS, Context.MODE_PRIVATE);
        return prefs.getBoolean(HAS_SIGNED_OUT, false);
    }

    public static void setHasBackedUpPhrase() {
        final SharedPreferences prefs = BaseApplication.get().getSharedPreferences(FileNames.USER_PREFS, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(HAS_BACKED_UP_PHRASE, true).apply();
    }

    public static boolean hasBackedUpPhrase() {
        final SharedPreferences prefs = BaseApplication.get().getSharedPreferences(FileNames.USER_PREFS, Context.MODE_PRIVATE);
        return prefs.getBoolean(HAS_BACKED_UP_PHRASE, false);
    }

    //Only clearing HAS_BACKED_UP_PHRASE and STORED_QR_CODE
    public static void clear() {
        final SharedPreferences prefs = BaseApplication.get().getSharedPreferences(FileNames.USER_PREFS, Context.MODE_PRIVATE);
        prefs
                .edit()
                .putBoolean(HAS_BACKED_UP_PHRASE, false)
                .putString(STORED_QR_CODE, null)
                .apply();
    }
}
