package com.tokenbrowser.presenter.factory;

import com.tokenbrowser.presenter.ViewProfilePresenter;

public final class ViewProfilePresenterFactory implements PresenterFactory<ViewProfilePresenter> {

    public ViewProfilePresenterFactory() {}

    @Override
    public ViewProfilePresenter create() {
        return new ViewProfilePresenter();
    }
}
