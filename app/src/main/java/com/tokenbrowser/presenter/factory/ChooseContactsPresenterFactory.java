package com.tokenbrowser.presenter.factory;

import com.tokenbrowser.presenter.ChooseContactPresenter;

public class ChooseContactsPresenterFactory implements PresenterFactory<ChooseContactPresenter> {
    @Override
    public ChooseContactPresenter create() {
        return new ChooseContactPresenter();
    }
}