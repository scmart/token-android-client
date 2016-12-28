package com.bakkenbaeck.token.presenter.factory;

import com.bakkenbaeck.token.presenter.QrPresenter;

public final class QrPresenterFactory implements PresenterFactory<QrPresenter> {

    @Override
    public QrPresenter create() {
        return new QrPresenter();
    }
}
