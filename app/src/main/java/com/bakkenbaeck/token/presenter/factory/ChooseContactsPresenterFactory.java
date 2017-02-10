package com.bakkenbaeck.token.presenter.factory;

import com.bakkenbaeck.token.presenter.ChooseContactPresenter;

public class ChooseContactsPresenterFactory implements PresenterFactory<ChooseContactPresenter> {
    @Override
    public ChooseContactPresenter create() {
        return new ChooseContactPresenter();
    }
}