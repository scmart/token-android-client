package com.bakkenbaeck.token.presenter.factory;

import com.bakkenbaeck.token.presenter.PlaceholderPresenter;

public final class PlaceholderPresenterFactory implements PresenterFactory<PlaceholderPresenter> {

    public PlaceholderPresenterFactory() {}

    @Override
    public PlaceholderPresenter create() {
        return new PlaceholderPresenter();
    }
}
