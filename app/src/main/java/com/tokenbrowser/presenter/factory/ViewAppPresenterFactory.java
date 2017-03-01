package com.tokenbrowser.presenter.factory;

import com.tokenbrowser.presenter.ViewAppPresenter;

public class ViewAppPresenterFactory implements PresenterFactory<ViewAppPresenter> {
    @Override
    public ViewAppPresenter create() {
        return new ViewAppPresenter();
    }
}
