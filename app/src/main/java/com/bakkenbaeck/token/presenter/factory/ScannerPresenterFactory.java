package com.bakkenbaeck.token.presenter.factory;

import com.bakkenbaeck.token.presenter.ScannerPresenter;

public final class ScannerPresenterFactory implements PresenterFactory<ScannerPresenter> {

    @Override
    public ScannerPresenter create() {
        return new ScannerPresenter();
    }
}
