package com.bakkenbaeck.token.presenter.factory;

import com.bakkenbaeck.token.presenter.SettingsContainerPresenter;

public final class SettingsContainerPresenterFactory implements PresenterFactory<SettingsContainerPresenter> {

    public SettingsContainerPresenterFactory() {}

    @Override
    public SettingsContainerPresenter create() {
        return new SettingsContainerPresenter();
    }
}
