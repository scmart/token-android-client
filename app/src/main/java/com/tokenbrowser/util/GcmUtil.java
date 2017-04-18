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

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.tokenbrowser.R;
import com.tokenbrowser.exception.InvalidGcmTokenException;
import com.tokenbrowser.view.BaseApplication;

import java.io.IOException;

import rx.Single;
import rx.schedulers.Schedulers;

public class GcmUtil {

    public static Single<String> getGcmToken() {
        return Single.fromCallable(() -> {
            final InstanceID instanceID = InstanceID.getInstance(BaseApplication.get());
            try {
                final String token =  instanceID.getToken(BaseApplication.get().getString(R.string.gcm_defaultSenderId),
                        GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
                if (token != null) return token;
                else throw new IOException();
            } catch (IOException e) {
                LogUtil.e("GcmUtil", "Error finding Gcm token " + e.toString());
                throw new InvalidGcmTokenException(e);
            }
        })
        .subscribeOn(Schedulers.io());
    }
}
