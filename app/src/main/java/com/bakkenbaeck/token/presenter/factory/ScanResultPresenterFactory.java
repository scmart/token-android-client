package com.bakkenbaeck.token.presenter.factory;

import com.bakkenbaeck.token.presenter.ScanResultPresenter;

public final class ScanResultPresenterFactory implements PresenterFactory<ScanResultPresenter> {

    @Override
    public ScanResultPresenter create() {
        return new ScanResultPresenter();
    }
}
