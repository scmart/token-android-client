package com.bakkenbaeck.token.presenter.factory;

import com.bakkenbaeck.token.presenter.SettingsPresenter;

public final class SettingsPresenterFactory implements PresenterFactory<SettingsPresenter> {

    public SettingsPresenterFactory() {}

    @Override
    public SettingsPresenter create() {
        return new SettingsPresenter();
    }
}
