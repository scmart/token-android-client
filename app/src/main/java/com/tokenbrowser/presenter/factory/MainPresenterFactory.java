package com.tokenbrowser.presenter.factory;

import com.tokenbrowser.presenter.MainPresenter;

public final class MainPresenterFactory implements PresenterFactory<MainPresenter> {

    public MainPresenterFactory() {}

    @Override
    public MainPresenter create() {
        return new MainPresenter();
    }
}
