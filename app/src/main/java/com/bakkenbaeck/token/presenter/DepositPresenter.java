package com.bakkenbaeck.token.presenter;

import com.bakkenbaeck.token.view.activity.DepositActivity;

public class DepositPresenter implements Presenter<DepositActivity> {

    private DepositActivity activity;

    @Override
    public void onViewAttached(DepositActivity view) {
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
