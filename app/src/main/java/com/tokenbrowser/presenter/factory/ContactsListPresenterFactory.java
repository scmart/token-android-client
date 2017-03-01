package com.tokenbrowser.presenter.factory;

import com.tokenbrowser.presenter.ContactsListPresenter;

public final class ContactsListPresenterFactory implements PresenterFactory<ContactsListPresenter> {

    public ContactsListPresenterFactory() {}

    @Override
    public ContactsListPresenter create() {
        return new ContactsListPresenter();
    }
}
