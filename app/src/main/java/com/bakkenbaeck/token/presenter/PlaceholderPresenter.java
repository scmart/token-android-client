package com.bakkenbaeck.token.presenter;

import com.bakkenbaeck.token.view.fragment.PlaceholderFragment;

public final class PlaceholderPresenter implements Presenter<PlaceholderFragment> {

    private PlaceholderFragment fragment;

    @Override
    public void onViewAttached(final PlaceholderFragment fragment) {
        this.fragment = fragment;
    }

    @Override
    public void onViewDetached() {
        this.fragment = null;
    }

    @Override
    public void onViewDestroyed() {
        this.fragment = null;
    }
}
