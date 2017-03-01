package com.tokenbrowser.presenter.factory;

import com.tokenbrowser.presenter.TrustedFriendsPresenter;

public class TrustedFriendsPresenterFactory implements PresenterFactory<TrustedFriendsPresenter> {
    @Override
    public TrustedFriendsPresenter create() {
        return new TrustedFriendsPresenter();
    }
}
