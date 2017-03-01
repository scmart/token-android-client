package com.tokenbrowser.presenter.factory;

import com.tokenbrowser.presenter.ContactsPresenter;

public final class ContactsPresenterFactory implements PresenterFactory<ContactsPresenter> {

    public ContactsPresenterFactory() {}

    @Override
    public ContactsPresenter create() {
        return new ContactsPresenter();
    }
}
