package com.bakkenbaeck.token.presenter.factory;

import com.bakkenbaeck.token.presenter.DepositPresenter;

public class DepositPresenterFactory implements PresenterFactory<DepositPresenter> {
    @Override
    public DepositPresenter create() {
        return new DepositPresenter();
    }
}
