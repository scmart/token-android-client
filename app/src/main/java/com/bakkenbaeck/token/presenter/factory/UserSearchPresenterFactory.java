package com.bakkenbaeck.token.presenter.factory;

import com.bakkenbaeck.token.presenter.UserSearchPresenter;

public final class UserSearchPresenterFactory implements PresenterFactory<UserSearchPresenter> {

    public UserSearchPresenterFactory() {}

    @Override
    public UserSearchPresenter create() {
        return new UserSearchPresenter();
    }
}
