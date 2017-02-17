package com.bakkenbaeck.token.presenter.factory;

import com.bakkenbaeck.token.presenter.LicensePresenter;

public class LicensePresenterFactory implements PresenterFactory<LicensePresenter> {
    @Override
    public LicensePresenter create() {
        return new LicensePresenter();
    }
}
