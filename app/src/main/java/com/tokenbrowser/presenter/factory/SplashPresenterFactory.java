package com.tokenbrowser.presenter.factory;

import com.tokenbrowser.presenter.SplashPresenter;

public class SplashPresenterFactory implements PresenterFactory<SplashPresenter> {
    @Override
    public SplashPresenter create() {
        return new SplashPresenter();
    }
}
