package com.tokenbrowser.presenter.factory;

import com.tokenbrowser.presenter.ProfilePresenter;

public final class ProfilePresenterFactory implements PresenterFactory<ProfilePresenter> {

    @Override
    public ProfilePresenter create() {
        return new ProfilePresenter();
    }
}
