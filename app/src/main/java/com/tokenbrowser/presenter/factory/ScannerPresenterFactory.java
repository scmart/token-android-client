package com.tokenbrowser.presenter.factory;

import com.tokenbrowser.presenter.ScannerPresenter;

public final class ScannerPresenterFactory implements PresenterFactory<ScannerPresenter> {

    @Override
    public ScannerPresenter create() {
        return new ScannerPresenter();
    }
}
