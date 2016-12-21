package com.bakkenbaeck.token.view.fragment.children;


import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bakkenbaeck.token.R;
import com.bakkenbaeck.token.databinding.FragmentContactsListBinding;
import com.bakkenbaeck.token.model.Contact;
import com.bakkenbaeck.token.presenter.ContactsListPresenter;
import com.bakkenbaeck.token.presenter.factory.ContactsListPresenterFactory;
import com.bakkenbaeck.token.presenter.factory.PresenterFactory;
import com.bakkenbaeck.token.view.adapter.listeners.OnItemClickListener;
import com.bakkenbaeck.token.view.fragment.BasePresenterFragment;

public class ContactsListFragment extends BasePresenterFragment<ContactsListPresenter, ContactsListFragment> {

    private ContactsListPresenter presenter;
    private FragmentContactsListBinding binding;
    private OnItemClickListener<Contact> onItemClickListener;

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
    protected void onPresenterPrepared(@NonNull final ContactsListPresenter presenter) {
        this.presenter = presenter;
        this.presenter.setOnItemClickListener(this.onItemClickListener);
    }

    public void setOnItemClickListener(final OnItemClickListener<Contact> onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
        if (this.presenter != null) {
            this.presenter.setOnItemClickListener(this.onItemClickListener);
        }
    }
}
