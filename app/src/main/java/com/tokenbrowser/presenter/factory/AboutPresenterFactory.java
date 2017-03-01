package com.tokenbrowser.presenter.factory;

import com.tokenbrowser.presenter.AboutPresenter;

public class AboutPresenterFactory implements PresenterFactory<AboutPresenter> {
    @Override
    public AboutPresenter create() {
        return new AboutPresenter();
    }
}
