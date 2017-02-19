package com.bakkenbaeck.token.presenter.factory;

import com.bakkenbaeck.token.presenter.TrustedFriendsPresenter;

public class TrustedFriendsPresenterFactory implements PresenterFactory<TrustedFriendsPresenter> {
    @Override
    public TrustedFriendsPresenter create() {
        return new TrustedFriendsPresenter();
    }
}
