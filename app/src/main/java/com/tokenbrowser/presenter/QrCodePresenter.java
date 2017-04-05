package com.tokenbrowser.presenter;

import com.tokenbrowser.view.activity.QrCodeActivity;

public class QrCodePresenter implements Presenter<QrCodeActivity> {

    private QrCodeActivity activity;

    @Override
    public void onViewAttached(QrCodeActivity view) {
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
