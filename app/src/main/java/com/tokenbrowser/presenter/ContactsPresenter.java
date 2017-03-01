package com.tokenbrowser.presenter;

import android.support.v4.app.FragmentTransaction;

import com.tokenbrowser.view.fragment.children.ContactsListFragment;
import com.tokenbrowser.view.fragment.toplevel.ContactsContainerFragment;

public final class ContactsPresenter implements
        Presenter<ContactsContainerFragment> {

    private ContactsListFragment contactsListFragment;

    private boolean firstTimeAttaching = true;

    @Override
    public void onViewAttached(final ContactsContainerFragment fragment) {

        if (this.firstTimeAttaching) {
            this.firstTimeAttaching = false;
            manuallyAddRootFragment(fragment);
        }
    }

    private void manuallyAddRootFragment(final ContactsContainerFragment fragment) {
        this.contactsListFragment = ContactsListFragment.newInstance();

        final FragmentTransaction transaction = fragment.getChildFragmentManager().beginTransaction();
        transaction.replace(fragment.getBinding().container.getId(), contactsListFragment).commit();
    }

    @Override
    public void onViewDetached() {}

    @Override
    public void onViewDestroyed() {}
}
