package com.bakkenbaeck.toshi.presenter.factory;


import com.bakkenbaeck.toshi.presenter.WithdrawPresenter;

public class WithdrawPresenterFactory implements PresenterFactory<WithdrawPresenter> {

    @Override
    public WithdrawPresenter create() {
        return new WithdrawPresenter();
    }
}
