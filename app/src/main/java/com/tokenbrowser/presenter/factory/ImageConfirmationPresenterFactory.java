package com.tokenbrowser.presenter.factory;

import com.tokenbrowser.presenter.ImageConfirmationPresenter;

public class ImageConfirmationPresenterFactory implements PresenterFactory<ImageConfirmationPresenter> {
    @Override
    public ImageConfirmationPresenter create() {
        return new ImageConfirmationPresenter();
    }
}
