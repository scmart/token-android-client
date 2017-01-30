package com.bakkenbaeck.token.presenter;

import com.bakkenbaeck.token.view.activity.AmountActivity;

public class AmountPresenter implements Presenter<AmountActivity> {

    private AmountActivity activity;

    @Override
    public void onViewAttached(AmountActivity view) {
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
