package com.tokenbrowser.view.fragment.toplevel;


import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.tokenbrowser.presenter.ContactsPresenter;
import com.tokenbrowser.presenter.factory.ContactsPresenterFactory;
import com.tokenbrowser.presenter.factory.PresenterFactory;
import com.tokenbrowser.token.R;
import com.tokenbrowser.token.databinding.FragmentContactsBinding;
import com.tokenbrowser.view.fragment.BasePresenterFragment;

public class ContactsFragment extends BasePresenterFragment<ContactsPresenter, ContactsFragment> {
    private FragmentContactsBinding binding;
    private ContactsPresenter presenter;

    public static ContactsFragment newInstance() {
        return new ContactsFragment();
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater,
                             final ViewGroup container,
                             final Bundle inState) {
        this.binding = DataBindingUtil.inflate(inflater, R.layout.fragment_contacts, container, false);
        initMenu();
        return binding.getRoot();
    }

    private void initMenu() {
        ((AppCompatActivity)getActivity()).setSupportActionBar(this.binding.toolbar);
        final ActionBar actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
        if (actionBar != null) actionBar.setDisplayShowTitleEnabled(false);
        setHasOptionsMenu(true);
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

    @Override
    protected int loaderId() {
        return 4001;
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
        inflater.inflate(R.menu.contacts, menu);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        this.presenter.handleActionMenuClicked(item);
        return super.onOptionsItemSelected(item);
    }
}
