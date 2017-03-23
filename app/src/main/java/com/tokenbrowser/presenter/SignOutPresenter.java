package com.tokenbrowser.presenter;

import com.tokenbrowser.view.activity.SignOutActivity;

public class SignOutPresenter implements Presenter<SignOutActivity> {

    private SignOutActivity activity;

    @Override
    public void onViewAttached(SignOutActivity view) {
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
