package com.bakkenbaeck.token.presenter.factory;

import com.bakkenbaeck.token.presenter.EditProfilePresenter;

public final class EditProfilePresenterFactory implements PresenterFactory<EditProfilePresenter> {

    public EditProfilePresenterFactory() {}

    @Override
    public EditProfilePresenter create() {
        return new EditProfilePresenter();
    }
}
