package com.bakkenbaeck.token.presenter.factory;

import com.bakkenbaeck.token.presenter.ContactsListPresenter;

public final class ContactsListPresenterFactory implements PresenterFactory<ContactsListPresenter> {

    public ContactsListPresenterFactory() {}

    @Override
    public ContactsListPresenter create() {
        return new ContactsListPresenter();
    }
}
