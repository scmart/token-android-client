package com.bakkenbaeck.token.presenter.factory;

import com.bakkenbaeck.token.presenter.ViewAppPresenter;

public class ViewAppPresenterFactory implements PresenterFactory<ViewAppPresenter> {
    @Override
    public ViewAppPresenter create() {
        return new ViewAppPresenter();
    }
}
