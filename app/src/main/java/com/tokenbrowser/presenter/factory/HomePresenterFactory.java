package com.tokenbrowser.presenter.factory;

import com.tokenbrowser.presenter.HomePresenter;

public class HomePresenterFactory implements PresenterFactory<HomePresenter> {

    @Override
    public HomePresenter create() {
        return new HomePresenter();
    }
}
