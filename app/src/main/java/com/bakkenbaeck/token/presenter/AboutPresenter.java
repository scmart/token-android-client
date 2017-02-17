package com.bakkenbaeck.token.presenter;

import com.bakkenbaeck.token.view.activity.AboutActivity;

public class AboutPresenter implements Presenter<AboutActivity> {

    private AboutActivity activity;

    @Override
    public void onViewAttached(AboutActivity view) {
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
