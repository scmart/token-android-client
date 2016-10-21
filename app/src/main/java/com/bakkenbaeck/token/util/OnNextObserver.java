package com.bakkenbaeck.token.util;


import rx.Observer;

public abstract class OnNextObserver<T> implements Observer<T> {

    @Override
    public void onCompleted() {
        final String programmerFriendlyMessage = "Unhandled onCompleted. This is potentially a programming error.";
        LogUtil.w(getClass(), programmerFriendlyMessage);
    }

    @Override
    public void onError(final Throwable e) {
        LogUtil.e(getClass(), "Unhandled onError " + e);
    }
}
