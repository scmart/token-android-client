package com.bakkenbaeck.toshi.presenter.factory;

import com.bakkenbaeck.toshi.presenter.Presenter;

public interface PresenterFactory<T extends Presenter> {
    T create();
}
