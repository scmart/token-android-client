package com.bakkenbaeck.token.util;


import rx.Observer;

public abstract class OnCompletedObserver<T> implements Observer<T> {

    @Override
    public void onError(final Throwable e) {
        // Todo - remove this toast
        //Toast.makeText(BaseApplication.get(), e.getMessage(), Toast.LENGTH_LONG).show();
        LogUtil.w(getClass(), "Unhandled onError " + e);
    }

    @Override
    public void onNext(final T t) {
        final String programmerFriendlyMessage = "Unhandled onNext. This is possibly a programming error.";
        LogUtil.e(getClass(), programmerFriendlyMessage);
    }
}
