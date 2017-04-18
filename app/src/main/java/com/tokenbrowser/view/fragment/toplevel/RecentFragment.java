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

import com.tokenbrowser.presenter.RecentPresenter;
import com.tokenbrowser.presenter.factory.PresenterFactory;
import com.tokenbrowser.presenter.factory.RecentPresenterFactory;
import com.tokenbrowser.R;
import com.tokenbrowser.databinding.FragmentRecentBinding;
import com.tokenbrowser.view.fragment.BasePresenterFragment;

public class RecentFragment extends BasePresenterFragment<RecentPresenter, RecentFragment> {

    private FragmentRecentBinding binding;
    private RecentPresenter presenter;

    public static RecentFragment newInstance() {
        return new RecentFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, final @Nullable Bundle inState) {
        this.binding = DataBindingUtil.inflate(inflater, R.layout.fragment_recent, container, false);
        initMenu();
        return binding.getRoot();
    }

    private void initMenu() {
        ((AppCompatActivity)getActivity()).setSupportActionBar(this.binding.toolbar);
        final ActionBar actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
        if (actionBar != null) actionBar.setDisplayShowTitleEnabled(false);
        setHasOptionsMenu(true);
    }

    public FragmentRecentBinding getBinding() {
        return this.binding;
    }

    @NonNull
    @Override
    protected PresenterFactory<RecentPresenter> getPresenterFactory() {
        return new RecentPresenterFactory();
    }

    @Override
    protected void onPresenterPrepared(@NonNull RecentPresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    protected int loaderId() {
        return hashCode();
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
