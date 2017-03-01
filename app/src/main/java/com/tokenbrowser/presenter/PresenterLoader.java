package com.tokenbrowser.presenter;

import android.content.Context;
import android.support.v4.content.Loader;

import com.tokenbrowser.presenter.factory.PresenterFactory;

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
        if (this.presenter != null) {
            deliverResult(this.presenter);
            return;
        }

        forceLoad();
    }

    @Override
    protected void onForceLoad() {
        this.presenter = this.factory.create();
        deliverResult(this.presenter);
    }

    @Override
    protected void onReset() {
        if (this.presenter != null) {
            this.presenter.onViewDestroyed();
            this.presenter = null;
        }
    }
}