package com.tokenbrowser.presenter;

import com.tokenbrowser.view.activity.FullscreenImageActivity;

public class FullscreenImagePresenter implements Presenter<FullscreenImageActivity> {

    private FullscreenImageActivity activity;

    @Override
    public void onViewAttached(FullscreenImageActivity view) {
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
