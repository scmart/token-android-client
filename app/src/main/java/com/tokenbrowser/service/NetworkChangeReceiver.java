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

package com.tokenbrowser.service;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.tokenbrowser.view.BaseApplication;

public class NetworkChangeReceiver extends BroadcastReceiver {

    private static final int NETWORK_STATUS_NOT_CONNECTED = 0;
    private static final int NETWORK_STATUS_WIFI = 1;
    private static final int NETWORK_STATUS_MOBILE =2;
    private static final int TYPE_WIFI = 1;
    private static final int TYPE_MOBILE = 2;
    private static final int TYPE_NOT_CONNECTED = 0;

    @Override
    public void onReceive(final Context context, final Intent intent) {
        if (!intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
            return;
        }
        
        final boolean isConnected = getCurrentConnectivityStatus(context);
        BaseApplication.get().isConnectedSubject().onNext(isConnected);
    }

    public static boolean getCurrentConnectivityStatus(final Context context) {
        final int status = getConnectivityStatusString(context);
        return status != NETWORK_STATUS_NOT_CONNECTED;
    }

    private static int getConnectivityStatusString(final Context context) {
        final int conn = getConnectivityStatus(context);
        if (conn == TYPE_WIFI) {
            return  NETWORK_STATUS_WIFI;
        } else if (conn == TYPE_MOBILE) {
            return NETWORK_STATUS_MOBILE;
        } else {
            return NETWORK_STATUS_NOT_CONNECTED;
        }
    }

    private static int getConnectivityStatus(final Context context) {
        final ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (null != activeNetwork) {
            if (activeNetwork.getType() == TYPE_WIFI) {
                return TYPE_WIFI;
            }

            if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                return TYPE_MOBILE;
            }
        }
        return TYPE_NOT_CONNECTED;
    }

}

