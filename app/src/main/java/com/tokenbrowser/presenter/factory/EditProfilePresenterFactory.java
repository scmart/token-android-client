package com.tokenbrowser.presenter.factory;

import com.tokenbrowser.presenter.EditProfilePresenter;

public final class EditProfilePresenterFactory implements PresenterFactory<EditProfilePresenter> {

    public EditProfilePresenterFactory() {}

    @Override
    public EditProfilePresenter create() {
        return new EditProfilePresenter();
    }
}
