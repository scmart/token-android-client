package com.bakkenbaeck.token.presenter.factory;

import com.bakkenbaeck.token.presenter.NavigationPresenter;

public final class NavigationPresenterFactory implements PresenterFactory<NavigationPresenter> {

    public NavigationPresenterFactory() {}

    @Override
    public NavigationPresenter create() {
        return new NavigationPresenter();
    }
}
