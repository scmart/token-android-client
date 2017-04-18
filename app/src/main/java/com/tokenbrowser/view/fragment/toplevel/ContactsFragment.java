/*
 * 	Copyright (c) 2017. Token Browser, Inc
 *
 * 	This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
import com.tokenbrowser.R;
import com.tokenbrowser.databinding.FragmentContactsBinding;
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
