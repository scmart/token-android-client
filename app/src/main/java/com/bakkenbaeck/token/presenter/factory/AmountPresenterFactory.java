package com.bakkenbaeck.token.presenter.factory;

import com.bakkenbaeck.token.presenter.AmountPresenter;

public class AmountPresenterFactory implements PresenterFactory<AmountPresenter> {
    @Override
    public AmountPresenter create() {
        return new AmountPresenter();
    }
}
