package com.bakkenbaeck.toshi.util;


import rx.Observer;

public abstract class OnNextObserver<T> implements Observer<T> {

    @Override
    public void onCompleted() {
        final String programmerFriendlyMessage = "Unhandled onCompleted. This is probably a programming error.";
        LogUtil.e(getClass(), programmerFriendlyMessage);
    }

    @Override
    public void onError(final Throwable e) {
        LogUtil.e(getClass(), "Unhandled onError " + e);
    }
}
