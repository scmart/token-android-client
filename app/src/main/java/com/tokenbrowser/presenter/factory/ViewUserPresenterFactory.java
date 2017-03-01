package com.tokenbrowser.presenter.factory;

import com.tokenbrowser.presenter.ViewUserPresenter;

public final class ViewUserPresenterFactory implements PresenterFactory<ViewUserPresenter> {

    @Override
    public ViewUserPresenter create() {
        return new ViewUserPresenter();
    }
}
