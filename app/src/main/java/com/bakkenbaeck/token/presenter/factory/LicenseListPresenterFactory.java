package com.bakkenbaeck.token.presenter.factory;

import com.bakkenbaeck.token.presenter.LicenseListPresenter;

public class LicenseListPresenterFactory implements PresenterFactory<LicenseListPresenter> {
    @Override
    public LicenseListPresenter create() {
        return new LicenseListPresenter();
    }
}
