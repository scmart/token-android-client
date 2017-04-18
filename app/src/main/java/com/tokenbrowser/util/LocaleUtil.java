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


import android.annotation.TargetApi;
import android.os.Build;

import com.tokenbrowser.view.BaseApplication;

import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class LocaleUtil {

    @TargetApi(Build.VERSION_CODES.N)
    public static Locale getLocale() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                return BaseApplication.get().getResources().getConfiguration().getLocales().get(0);
            } else {
                return BaseApplication.get().getResources().getConfiguration().locale;
            }
        } catch (final NullPointerException ex) {
            LogUtil.e(LocaleUtil.class, "NPE when getting locale. " + ex);
            // Default to something!
            return Locale.ENGLISH;
        }
    }

    public static DecimalFormatSymbols getDecimalFormatSymbols() {
        return new DecimalFormatSymbols(getLocale());
    }
}
