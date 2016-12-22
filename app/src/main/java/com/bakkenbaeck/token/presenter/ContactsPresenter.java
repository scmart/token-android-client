package com.bakkenbaeck.token.presenter;

import android.support.v4.app.FragmentTransaction;

import com.bakkenbaeck.token.view.fragment.children.ContactsListFragment;
import com.bakkenbaeck.token.view.fragment.toplevel.ContactsFragment;

public final class ContactsPresenter implements
        Presenter<ContactsFragment> {

    private ContactsListFragment contactsListFragment;

    private boolean firstTimeAttaching = true;

    @Override
    public void onViewAttached(final ContactsFragment fragment) {

        if (this.firstTimeAttaching) {
            this.firstTimeAttaching = false;
            manuallyAddRootFragment(fragment);
        }
    }

    private void manuallyAddRootFragment(final ContactsFragment fragment) {
        this.contactsListFragment = ContactsListFragment.newInstance();

        final FragmentTransaction transaction = fragment.getChildFragmentManager().beginTransaction();
        transaction.replace(fragment.getBinding().container.getId(), contactsListFragment).commit();
    }

    @Override
    public void onViewDetached() {}

    @Override
    public void onViewDestroyed() {}
}
