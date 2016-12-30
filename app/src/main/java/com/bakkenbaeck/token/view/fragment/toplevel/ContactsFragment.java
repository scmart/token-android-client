package com.bakkenbaeck.token.view.fragment.toplevel;


import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bakkenbaeck.token.R;
import com.bakkenbaeck.token.databinding.FragmentContactsBinding;
import com.bakkenbaeck.token.presenter.ContactsPresenter;
import com.bakkenbaeck.token.presenter.factory.ContactsPresenterFactory;
import com.bakkenbaeck.token.presenter.factory.PresenterFactory;
import com.bakkenbaeck.token.view.fragment.BasePresenterFragment;

public class ContactsFragment extends BasePresenterFragment<ContactsPresenter, ContactsFragment> {

    private FragmentContactsBinding binding;

    public static ContactsFragment newInstance() {
        return new ContactsFragment();
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater,
                             final ViewGroup container,
                             final Bundle inState) {
        this.binding = DataBindingUtil.inflate(inflater, R.layout.fragment_contacts, container, false);
        this.binding.title.setText(R.string.tab_4);
        return binding.getRoot();
    }

    public FragmentContactsBinding getBinding() {
        return this.binding;
    }

    @NonNull
    @Override
    protected PresenterFactory<ContactsPresenter> getPresenterFactory() {
        return new ContactsPresenterFactory();
    }

    @Override
    protected void onPresenterPrepared(@NonNull final ContactsPresenter presenter) {}

    @Override
    protected int loaderId() {
        return 3;
    }
}
