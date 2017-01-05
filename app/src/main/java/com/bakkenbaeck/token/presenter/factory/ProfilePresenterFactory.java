package com.bakkenbaeck.token.presenter.factory;

import com.bakkenbaeck.token.presenter.ProfilePresenter;

public final class ProfilePresenterFactory implements PresenterFactory<ProfilePresenter> {

    @Override
    public ProfilePresenter create() {
        return new ProfilePresenter();
    }
}
