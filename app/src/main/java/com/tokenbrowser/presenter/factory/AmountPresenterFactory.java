package com.tokenbrowser.presenter.factory;

import com.tokenbrowser.presenter.AmountPresenter;

public class AmountPresenterFactory implements PresenterFactory<AmountPresenter> {
    @Override
    public AmountPresenter create() {
        return new AmountPresenter();
    }
}
