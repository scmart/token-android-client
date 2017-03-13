package com.tokenbrowser.presenter.factory;

import com.tokenbrowser.presenter.FullscreenImagePresenter;

public class FullscreenImagePresenterFactory implements PresenterFactory<FullscreenImagePresenter> {
    @Override
    public FullscreenImagePresenter create() {
        return new FullscreenImagePresenter();
    }
}
