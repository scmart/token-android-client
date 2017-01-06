package com.bakkenbaeck.token.presenter.factory;

import com.bakkenbaeck.token.presenter.ViewProfilePresenter;

public final class ViewProfilePresenterFactory implements PresenterFactory<ViewProfilePresenter> {

    public ViewProfilePresenterFactory() {}

    @Override
    public ViewProfilePresenter create() {
        return new ViewProfilePresenter();
    }
}
