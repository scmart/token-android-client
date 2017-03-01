package com.tokenbrowser.presenter.factory;

import com.tokenbrowser.presenter.SettingsPresenter;

public final class SettingsPresenterFactory implements PresenterFactory<SettingsPresenter> {

    public SettingsPresenterFactory() {}

    @Override
    public SettingsPresenter create() {
        return new SettingsPresenter();
    }
}
