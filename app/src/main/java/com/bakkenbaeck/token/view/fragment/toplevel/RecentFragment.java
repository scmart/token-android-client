package com.bakkenbaeck.token.view.fragment.toplevel;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bakkenbaeck.token.R;
import com.bakkenbaeck.token.databinding.FragmentRecentBinding;
import com.bakkenbaeck.token.presenter.RecentPresenter;
import com.bakkenbaeck.token.presenter.factory.PresenterFactory;
import com.bakkenbaeck.token.presenter.factory.RecentPresenterFactory;
import com.bakkenbaeck.token.view.fragment.BasePresenterFragment;

public class RecentFragment extends BasePresenterFragment<RecentPresenter, RecentFragment> {

    private FragmentRecentBinding binding;

    public static RecentFragment newInstance() {
        return new RecentFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, final @Nullable Bundle inState) {
        this.binding = DataBindingUtil.inflate(inflater, R.layout.fragment_recent, container, false);
        return binding.getRoot();
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

    }

    @Override
    protected int loaderId() {
        return hashCode();
    }
}
