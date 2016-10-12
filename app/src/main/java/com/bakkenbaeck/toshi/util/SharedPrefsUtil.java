package com.bakkenbaeck.toshi.util;

import android.content.SharedPreferences;

import com.bakkenbaeck.toshi.view.BaseApplication;
import com.securepreferences.SecurePreferences;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class SharedPrefsUtil {
    public static final String IS_VERIFIED = "SharedPrefsUtil";

    public static Observable<Boolean> isVerified(){
        return Observable.create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(Subscriber<? super Boolean> subscriber) {
                SharedPreferences prefs = new SecurePreferences(BaseApplication.get(), "", "um");
                boolean b = prefs.getBoolean(IS_VERIFIED, false);
                subscriber.onNext(b);
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public static void saveVerified(boolean b){
        SharedPreferences prefs = new SecurePreferences(BaseApplication.get(), "", "um");
        prefs.edit().putBoolean(IS_VERIFIED, b).apply();
    }
}
