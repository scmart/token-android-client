package com.bakkenbaeck.token.presenter;

import android.support.v4.app.FragmentTransaction;

import com.bakkenbaeck.token.view.fragment.children.ContactsListFragment;
import com.bakkenbaeck.token.view.fragment.toplevel.ContactsFragment;

public final class ContactsPresenter implements Presenter<ContactsFragment> {

    private ContactsFragment fragment;
    private boolean firstTimeAttaching = true;

    @Override
    public void onViewAttached(final ContactsFragment fragment) {
        this.fragment = fragment;

        if (this.firstTimeAttaching) {
            this.firstTimeAttaching = false;

            final FragmentTransaction transaction = fragment.getChildFragmentManager().beginTransaction();
            transaction.replace(fragment.getBinding().container.getId(), ContactsListFragment.newInstance()).commit();
        }
    }

    @Override
    public void onViewDetached() {
        this.fragment = null;
    }

    @Override
    public void onViewDestroyed() {
        this.fragment = null;
    }
}
