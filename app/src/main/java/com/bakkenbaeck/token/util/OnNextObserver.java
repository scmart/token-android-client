package com.bakkenbaeck.token.util;


import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.bakkenbaeck.token.view.BaseApplication;

import rx.Observer;

public abstract class OnNextObserver<T> implements Observer<T> {

    @Override
    public void onCompleted() {
        final String programmerFriendlyMessage = "Unhandled onCompleted. This is potentially a programming error.";
        LogUtil.w(getClass(), programmerFriendlyMessage);
    }

    @Override
    public void onError(final Throwable e) {
        // Todo - remove this toast
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(BaseApplication.get(), e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

        LogUtil.e(getClass(), "Unhandled onError " + e);
    }
}
