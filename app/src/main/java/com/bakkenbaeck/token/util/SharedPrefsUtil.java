package com.bakkenbaeck.token.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.bakkenbaeck.token.R;
import com.bakkenbaeck.token.view.BaseApplication;

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
                final SharedPreferences prefs = BaseApplication.get().getSharedPreferences(BaseApplication.get().getResources().getString(R.string.user_manager_pref_filename), Context.MODE_PRIVATE);
                final boolean b = prefs.getBoolean(IS_VERIFIED, false);
                subscriber.onNext(b);
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public static void saveIsVerified(final boolean isVerified){
        final SharedPreferences prefs = BaseApplication.get().getSharedPreferences(BaseApplication.get().getResources().getString(R.string.user_manager_pref_filename), Context.MODE_PRIVATE);
        prefs.edit().putBoolean(IS_VERIFIED, isVerified).apply();
    }
}
