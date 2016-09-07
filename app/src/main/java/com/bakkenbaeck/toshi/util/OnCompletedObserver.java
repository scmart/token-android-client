package com.bakkenbaeck.toshi.util;


import rx.Observer;

public abstract class OnCompletedObserver<T> implements Observer<T> {

    @Override
    public void onError(final Throwable e) {
        LogUtil.w(getClass(), "Unhandled onError " + e);
    }

    @Override
    public void onNext(final T t) {
        final String programmerFriendlyMessage = "Unhandled onNext. This is possibly a programming error.";
        LogUtil.e(getClass(), programmerFriendlyMessage);
    }
}
