package com.tokenbrowser.view.fragment.toplevel;


import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tokenbrowser.R;
import com.tokenbrowser.databinding.FragmentSettingsBinding;
import com.tokenbrowser.presenter.SettingsPresenter;
import com.tokenbrowser.presenter.factory.PresenterFactory;
import com.tokenbrowser.presenter.factory.SettingsPresenterFactory;
import com.tokenbrowser.view.fragment.BasePresenterFragment;

public class SettingsFragment extends BasePresenterFragment<SettingsPresenter, SettingsFragment> {

    private FragmentSettingsBinding binding;

    public static SettingsFragment newInstance() {
        return new SettingsFragment();
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater,
                             final ViewGroup container,
                             final Bundle inState) {
        this.binding = DataBindingUtil.inflate(inflater, R.layout.fragment_settings, container, false);
        return binding.getRoot();
    }

    public FragmentSettingsBinding getBinding() {
        return this.binding;
    }

    @NonNull
    @Override
    protected PresenterFactory<SettingsPresenter> getPresenterFactory() {
        return new SettingsPresenterFactory();
    }

    @Override
    protected void onPresenterPrepared(@NonNull final SettingsPresenter presenter) {

    }

    @Override
    protected int loaderId() {
        return 5001;
    }
}
