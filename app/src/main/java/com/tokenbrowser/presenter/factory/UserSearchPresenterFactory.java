package com.tokenbrowser.presenter.factory;

import com.tokenbrowser.presenter.UserSearchPresenter;

public final class UserSearchPresenterFactory implements PresenterFactory<UserSearchPresenter> {

    public UserSearchPresenterFactory() {}

    @Override
    public UserSearchPresenter create() {
        return new UserSearchPresenter();
    }
}
