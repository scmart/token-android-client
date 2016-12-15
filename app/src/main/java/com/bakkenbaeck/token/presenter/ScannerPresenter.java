package com.bakkenbaeck.token.presenter;

import com.bakkenbaeck.token.view.fragment.ScannerFragment;

public final class ScannerPresenter implements Presenter<ScannerFragment> {

    private ScannerFragment fragment;

    @Override
    public void onViewAttached(final ScannerFragment fragment) {
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
