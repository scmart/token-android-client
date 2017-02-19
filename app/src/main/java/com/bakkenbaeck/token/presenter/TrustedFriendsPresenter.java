package com.bakkenbaeck.token.presenter;

import com.bakkenbaeck.token.view.activity.TrustedFriendsActivity;

public class TrustedFriendsPresenter implements Presenter<TrustedFriendsActivity> {

    private TrustedFriendsActivity activity;

    @Override
    public void onViewAttached(TrustedFriendsActivity view) {
        this.activity = view;
    }

    @Override
    public void onViewDetached() {
        this.activity = null;
    }

    @Override
    public void onViewDestroyed() {
        this.activity = null;
    }
}
