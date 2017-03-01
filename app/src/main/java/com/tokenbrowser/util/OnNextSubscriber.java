package com.tokenbrowser.util;


import rx.Subscriber;

public abstract class OnNextSubscriber<T> extends Subscriber<T> {

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
