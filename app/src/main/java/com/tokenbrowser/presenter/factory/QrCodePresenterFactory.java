package com.tokenbrowser.presenter.factory;

import com.tokenbrowser.presenter.QrCodePresenter;

public class QrCodePresenterFactory implements PresenterFactory<QrCodePresenter> {
    @Override
    public QrCodePresenter create() {
        return new QrCodePresenter();
    }
}
