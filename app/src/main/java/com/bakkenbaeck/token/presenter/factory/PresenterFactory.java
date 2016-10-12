package com.bakkenbaeck.token.presenter.factory;

import com.bakkenbaeck.token.presenter.Presenter;

public interface PresenterFactory<T extends Presenter> {
    T create();
}
