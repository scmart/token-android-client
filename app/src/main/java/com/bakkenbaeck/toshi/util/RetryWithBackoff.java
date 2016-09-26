package com.bakkenbaeck.toshi.util;


import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.functions.Func1;

public class RetryWithBackoff implements Func1<Observable<? extends Throwable>, Observable<?>> {

    private final int retryDelayMillis = 1000;
    private final int maxRetryDelayMillis = 15000;
    private final int maxRetries;
    private int retryCount;

    public RetryWithBackoff() {
        this(7);
    }

    public RetryWithBackoff(final int maxRetries) {
        this.retryCount = 0;
        this.maxRetries = maxRetries;
    }

    @Override
    public Observable<?> call(Observable<? extends Throwable> attempts) {
        return attempts
                .flatMap(new Func1<Throwable, Observable<?>>() {
                    @Override
                    public Observable<?> call(Throwable throwable) {
                        if (++retryCount < maxRetries) {
                            final int backedOffRetryDelay = Math.min(retryDelayMillis * retryCount, maxRetryDelayMillis);
                            return Observable.timer(backedOffRetryDelay, TimeUnit.MILLISECONDS);
                        }

                        // Max retries hit. Just pass the error along.
                        return Observable.error(throwable);
                    }
                });
    }
}
