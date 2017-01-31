package com.bakkenbaeck.token.presenter.factory;

import com.bakkenbaeck.token.presenter.HomePresenter;

public class HomePresenterFactory implements PresenterFactory<HomePresenter> {

    @Override
    public HomePresenter create() {
        return new HomePresenter();
    }
}
