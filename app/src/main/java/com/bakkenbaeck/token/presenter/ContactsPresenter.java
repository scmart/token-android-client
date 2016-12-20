package com.bakkenbaeck.token.presenter;

import android.support.v4.app.FragmentTransaction;
import android.widget.Toast;

import com.bakkenbaeck.token.model.Contact;
import com.bakkenbaeck.token.view.adapter.listeners.OnItemClickListener;
import com.bakkenbaeck.token.view.fragment.children.ContactsListFragment;
import com.bakkenbaeck.token.view.fragment.toplevel.ContactsFragment;

public final class ContactsPresenter implements
        Presenter<ContactsFragment>,
        OnItemClickListener<Contact> {

    private ContactsFragment fragment;
    private ContactsListFragment contactsListFragment;
    private boolean firstTimeAttaching = true;

    @Override
    public void onViewAttached(final ContactsFragment fragment) {
        this.fragment = fragment;

        if (this.firstTimeAttaching) {
            this.firstTimeAttaching = false;
            this.contactsListFragment = ContactsListFragment.newInstance();

            final FragmentTransaction transaction = fragment.getChildFragmentManager().beginTransaction();
            transaction.replace(fragment.getBinding().container.getId(), contactsListFragment).commit();
        }
        this.contactsListFragment.setOnItemClickListener(this);
    }

    @Override
    public void onViewDetached() {
        this.fragment = null;
    }

    @Override
    public void onViewDestroyed() {
        this.fragment = null;
    }

    @Override
    public void onItemClick(final Contact contact) {
        Toast.makeText(fragment.getContext(), contact.getName(), Toast.LENGTH_SHORT).show();
    }
}
