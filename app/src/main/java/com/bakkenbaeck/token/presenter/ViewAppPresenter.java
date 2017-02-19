package com.bakkenbaeck.token.presenter;

import com.bakkenbaeck.token.model.network.App;
import com.bakkenbaeck.token.view.activity.ViewAppActivity;

public class ViewAppPresenter implements Presenter<ViewAppActivity> {

    private ViewAppActivity activity;
    private App app;

    @Override
    public void onViewAttached(ViewAppActivity view) {
        this.activity = view;
        getIntentData();
    }

    private void getIntentData() {
        this.app = this.activity.getIntent().getParcelableExtra(ViewAppActivity.APP);
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
