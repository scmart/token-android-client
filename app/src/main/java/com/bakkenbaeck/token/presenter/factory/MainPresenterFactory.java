package com.bakkenbaeck.token.presenter.factory;

import com.bakkenbaeck.token.presenter.MainPresenter;

public final class MainPresenterFactory implements PresenterFactory<MainPresenter> {

    public MainPresenterFactory() {}

    @Override
    public MainPresenter create() {
        return new MainPresenter();
    }
}
