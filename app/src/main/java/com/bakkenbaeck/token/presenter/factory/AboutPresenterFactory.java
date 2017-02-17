package com.bakkenbaeck.token.presenter.factory;

import com.bakkenbaeck.token.presenter.AboutPresenter;

public class AboutPresenterFactory implements PresenterFactory<AboutPresenter> {
    @Override
    public AboutPresenter create() {
        return new AboutPresenter();
    }
}
