package com.bakkenbaeck.token.view.fragment;


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

public class ContactsFragment extends BasePresenterFragment<ContactsPresenter, ContactsFragment> {

    private ContactsPresenter presenter;
    private FragmentContactsBinding binding;

    public static ContactsFragment newInstance(final CharSequence title) {
        final ContactsFragment f = new ContactsFragment();
        final Bundle b = new Bundle();
        b.putCharSequence("title", title);
        f.setArguments(b);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater,
                             final ViewGroup container,
                             final Bundle inState) {
        final CharSequence title = getArguments().getCharSequence("title", null);
        this.binding = DataBindingUtil.inflate(inflater, R.layout.fragment_contacts, container, false);
        this.binding.title.setText(title);
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
    protected void onPresenterPrepared(@NonNull final ContactsPresenter presenter) {
        this.presenter = presenter;
    }
}
