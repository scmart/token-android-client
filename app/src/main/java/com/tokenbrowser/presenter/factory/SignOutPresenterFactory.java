package com.tokenbrowser.presenter.factory;

import com.tokenbrowser.presenter.SignOutPresenter;

public class SignOutPresenterFactory implements PresenterFactory<SignOutPresenter> {
    @Override
    public SignOutPresenter create() {
        return new SignOutPresenter();
    }
}
