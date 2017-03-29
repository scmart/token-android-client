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
