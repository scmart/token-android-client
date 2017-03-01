package com.tokenbrowser.view.fragment.children;


import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tokenbrowser.token.R;
import com.tokenbrowser.token.databinding.FragmentContactsListBinding;
import com.tokenbrowser.presenter.ContactsListPresenter;
import com.tokenbrowser.presenter.factory.ContactsListPresenterFactory;
import com.tokenbrowser.presenter.factory.PresenterFactory;
import com.tokenbrowser.view.fragment.BasePresenterFragment;

public class ContactsListFragment extends BasePresenterFragment<ContactsListPresenter, ContactsListFragment> {
    private FragmentContactsListBinding binding;

    public static ContactsListFragment newInstance() {
        return new ContactsListFragment();
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater,
                             final ViewGroup container,
                             final Bundle inState) {
        this.binding = DataBindingUtil.inflate(inflater, R.layout.fragment_contacts_list, container, false);
        return binding.getRoot();
    }

    public FragmentContactsListBinding getBinding() {
        return this.binding;
    }

    @NonNull
    @Override
    protected PresenterFactory<ContactsListPresenter> getPresenterFactory() {
        return new ContactsListPresenterFactory();
    }

    @Override
    protected void onPresenterPrepared(@NonNull final ContactsListPresenter presenter) {}

    @Override
    protected int loaderId() {
        return 4001;
    }
}
