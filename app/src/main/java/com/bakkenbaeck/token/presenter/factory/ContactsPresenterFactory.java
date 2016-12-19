package com.bakkenbaeck.token.presenter.factory;

import com.bakkenbaeck.token.presenter.ContactsPresenter;

public final class ContactsPresenterFactory implements PresenterFactory<ContactsPresenter> {

    public ContactsPresenterFactory() {}

    @Override
    public ContactsPresenter create() {
        return new ContactsPresenter();
    }
}
