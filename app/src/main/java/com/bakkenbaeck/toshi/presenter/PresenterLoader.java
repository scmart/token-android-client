package com.bakkenbaeck.toshi.presenter;

import android.content.Context;
import android.support.v4.content.Loader;

import com.bakkenbaeck.toshi.presenter.factory.PresenterFactory;

public class PresenterLoader<T extends Presenter> extends Loader<T> {

    private final PresenterFactory<T> factory;
    private T presenter;

    public PresenterLoader(final Context context, final PresenterFactory<T> factory) {
        super(context);
        this.factory = factory;
    }

    @Override
    protected void onStartLoading() {
        // Returns the presenter if one exists
        // otherwise it loads and returns a new presenter
        if (presenter != null) {
            deliverResult(presenter);
            return;
        }

        forceLoad();
    }

    @Override
    protected void onForceLoad() {
        presenter = factory.create();
        deliverResult(presenter);
    }

    @Override
    protected void onReset() {
        if (presenter != null) {
            presenter.onViewDestroyed();
            presenter = null;
        }
    }
}