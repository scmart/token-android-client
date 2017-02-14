package com.bakkenbaeck.token.presenter.factory;

import com.bakkenbaeck.token.presenter.RecentPresenter;

public final class RecentPresenterFactory implements PresenterFactory<RecentPresenter> {

    public RecentPresenterFactory() {}

    @Override
    public RecentPresenter create() {
        return new RecentPresenter();
    }
}
