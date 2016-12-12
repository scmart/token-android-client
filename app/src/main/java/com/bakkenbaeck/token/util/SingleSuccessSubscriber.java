package com.bakkenbaeck.token.util;


import rx.SingleSubscriber;

public abstract class SingleSuccessSubscriber<T> extends SingleSubscriber<T> {

    @Override
    public void onError(final Throwable error) {
        LogUtil.e(getClass(), error.getMessage());
        this.unsubscribe();
    }
}
