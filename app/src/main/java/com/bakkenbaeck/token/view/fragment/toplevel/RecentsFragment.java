package com.bakkenbaeck.token.view.fragment.toplevel;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bakkenbaeck.token.R;
import com.bakkenbaeck.token.databinding.FragmentRecentsBinding;
import com.bakkenbaeck.token.presenter.RecentsPresenter;
import com.bakkenbaeck.token.presenter.factory.PresenterFactory;
import com.bakkenbaeck.token.presenter.factory.RecentsPresenterFactory;
import com.bakkenbaeck.token.view.fragment.BasePresenterFragment;

public class RecentsFragment extends BasePresenterFragment<RecentsPresenter, RecentsFragment> {

    private FragmentRecentsBinding binding;

    public static RecentsFragment newInstance() {
        return new RecentsFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, final @Nullable Bundle inState) {
        this.binding = DataBindingUtil.inflate(inflater, R.layout.fragment_recents, container, false);
        return binding.getRoot();
    }

    public FragmentRecentsBinding getBinding() {
        return this.binding;
    }

    @NonNull
    @Override
    protected PresenterFactory<RecentsPresenter> getPresenterFactory() {
        return new RecentsPresenterFactory();
    }

    @Override
    protected void onPresenterPrepared(@NonNull RecentsPresenter presenter) {

    }

    @Override
    protected int loaderId() {
        return hashCode();
    }
}
