package com.tokenbrowser.presenter.factory;

import com.tokenbrowser.presenter.AppsPresenter;

public class AppsPresenterFactory implements PresenterFactory<AppsPresenter> {
    @Override
    public AppsPresenter create() {
        return new AppsPresenter();
    }
}
