package com.tokenbrowser.presenter;

import com.tokenbrowser.view.activity.ImageConfirmationActivity;

public class ImageConfirmationPresenter implements Presenter<ImageConfirmationActivity> {

    private ImageConfirmationActivity activity;

    @Override
    public void onViewAttached(ImageConfirmationActivity view) {
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
