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
import com.tokenbrowser.token.R;
import com.tokenbrowser.token.databinding.FragmentRecentBinding;
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
