package com.tokenbrowser.presenter.factory;

import com.tokenbrowser.presenter.LicenseListPresenter;

public class LicenseListPresenterFactory implements PresenterFactory<LicenseListPresenter> {
    @Override
    public LicenseListPresenter create() {
        return new LicenseListPresenter();
    }
}
