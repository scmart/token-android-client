package com.tokenbrowser.presenter.factory;

import com.tokenbrowser.presenter.LicensePresenter;

public class LicensePresenterFactory implements PresenterFactory<LicensePresenter> {
    @Override
    public LicensePresenter create() {
        return new LicensePresenter();
    }
}
