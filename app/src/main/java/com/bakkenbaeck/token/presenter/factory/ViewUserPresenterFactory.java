package com.bakkenbaeck.token.presenter.factory;

import com.bakkenbaeck.token.presenter.ViewUserPresenter;

public final class ViewUserPresenterFactory implements PresenterFactory<ViewUserPresenter> {

    @Override
    public ViewUserPresenter create() {
        return new ViewUserPresenter();
    }
}
