package com.bakkenbaeck.token.presenter.factory;


import com.bakkenbaeck.token.presenter.WithdrawPresenter;

public class WithdrawPresenterFactory implements PresenterFactory<WithdrawPresenter> {

    @Override
    public WithdrawPresenter create() {
        return new WithdrawPresenter();
    }
}
