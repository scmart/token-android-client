package com.bakkenbaeck.toshi.util;


import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.functions.Func1;

public class RetryWithBackoff implements Func1<Observable<? extends Throwable>, Observable<?>> {

    private final int retryDelayMillis = 1000;
    private final int maxRetryDelayMillis = 15000;
    private int retryCount;

    public RetryWithBackoff() {
        this.retryCount = 0;
    }

    @Override
    public Observable<?> call(Observable<? extends Throwable> attempts) {
        return attempts
                .flatMap(new Func1<Throwable, Observable<?>>() {
                    @Override
                    public Observable<?> call(Throwable throwable) {
                        retryCount++;
                        final int backedOffRetryDelay = Math.min(retryDelayMillis * retryCount, maxRetryDelayMillis);
                        return Observable.timer(backedOffRetryDelay, TimeUnit.MILLISECONDS);
                    }
                });
    }
}
