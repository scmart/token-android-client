package com.tokenbrowser.presenter.factory;

import com.tokenbrowser.presenter.SignInPresenter;

public class SignInPresenterFactory implements PresenterFactory<SignInPresenter> {
    @Override
    public SignInPresenter create() {
        return new SignInPresenter();
    }
}
