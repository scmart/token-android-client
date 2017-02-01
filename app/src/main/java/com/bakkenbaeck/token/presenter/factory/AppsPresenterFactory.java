package com.bakkenbaeck.token.presenter.factory;

import com.bakkenbaeck.token.presenter.AppsPresenter;

public class AppsPresenterFactory implements PresenterFactory<AppsPresenter> {
    @Override
    public AppsPresenter create() {
        return new AppsPresenter();
    }
}
