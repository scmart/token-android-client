package com.bakkenbaeck.token.presenter.factory;

import com.bakkenbaeck.token.presenter.RecentsPresenter;

public final class RecentsPresenterFactory implements PresenterFactory<RecentsPresenter> {

    public RecentsPresenterFactory() {}

    @Override
    public RecentsPresenter create() {
        return new RecentsPresenter();
    }
}
