package com.tokenbrowser.presenter.factory;

import com.tokenbrowser.presenter.DepositPresenter;

public class DepositPresenterFactory implements PresenterFactory<DepositPresenter> {
    @Override
    public DepositPresenter create() {
        return new DepositPresenter();
    }
}
