package com.tokenbrowser.presenter.factory;

import com.tokenbrowser.presenter.SettingsContainerPresenter;

public final class SettingsContainerPresenterFactory implements PresenterFactory<SettingsContainerPresenter> {

    public SettingsContainerPresenterFactory() {}

    @Override
    public SettingsContainerPresenter create() {
        return new SettingsContainerPresenter();
    }
}
