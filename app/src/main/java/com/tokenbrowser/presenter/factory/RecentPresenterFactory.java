package com.tokenbrowser.presenter.factory;

import com.tokenbrowser.presenter.RecentPresenter;

public final class RecentPresenterFactory implements PresenterFactory<RecentPresenter> {

    public RecentPresenterFactory() {}

    @Override
    public RecentPresenter create() {
        return new RecentPresenter();
    }
}
