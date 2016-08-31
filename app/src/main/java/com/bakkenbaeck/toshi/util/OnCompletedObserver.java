package com.bakkenbaeck.toshi.util;


import rx.Observer;

public abstract class OnCompletedObserver<T> implements Observer<T> {

    @Override
    public void onError(final Throwable e) {
        LogUtil.e(getClass(), "Unhandled onError " + e);
    }

    @Override
    public void onNext(final T t) {
        final String programmerFriendlyMessage = "Unhandled onNext. This is probably a programming error.";
        LogUtil.e(getClass(), programmerFriendlyMessage);
        throw new RuntimeException(programmerFriendlyMessage);
    }
}
