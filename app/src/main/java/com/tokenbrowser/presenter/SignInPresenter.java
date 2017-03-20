package com.tokenbrowser.presenter;

import com.tokenbrowser.view.activity.SignInActivity;

public class SignInPresenter implements Presenter<SignInActivity> {

    private SignInActivity activity;

    @Override
    public void onViewAttached(SignInActivity view) {
        this.activity = view;
    }

    @Override
    public void onViewDetached() {
        this.activity = null;
    }

    @Override
    public void onDestroyed() {
        this.activity = null;
    }
}
