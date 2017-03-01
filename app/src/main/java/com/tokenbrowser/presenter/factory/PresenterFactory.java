package com.tokenbrowser.presenter.factory;

import com.tokenbrowser.presenter.Presenter;

public interface PresenterFactory<T extends Presenter> {
    T create();
}
