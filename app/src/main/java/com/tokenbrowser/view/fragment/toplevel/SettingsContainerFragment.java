package com.tokenbrowser.view.fragment.toplevel;


import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tokenbrowser.R;
import com.tokenbrowser.databinding.FragmentTopLevelBinding;
import com.tokenbrowser.presenter.SettingsContainerPresenter;
import com.tokenbrowser.presenter.factory.PresenterFactory;
import com.tokenbrowser.presenter.factory.SettingsContainerPresenterFactory;
import com.tokenbrowser.view.fragment.BasePresenterFragment;

public class SettingsContainerFragment extends BasePresenterFragment<SettingsContainerPresenter, SettingsContainerFragment> {

    private FragmentTopLevelBinding binding;

    public static SettingsContainerFragment newInstance() {
        return new SettingsContainerFragment();
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater,
                             final ViewGroup container,
                             final Bundle inState) {
        this.binding = DataBindingUtil.inflate(inflater, R.layout.fragment_top_level, container, false);
        this.binding.title.setText(R.string.tab_5);
        return binding.getRoot();
    }

    public FragmentTopLevelBinding getBinding() {
        return this.binding;
    }

    @NonNull
    @Override
    protected PresenterFactory<SettingsContainerPresenter> getPresenterFactory() {
        return new SettingsContainerPresenterFactory();
    }

    @Override
    protected void onPresenterPrepared(@NonNull final SettingsContainerPresenter presenter) {}

    @Override
    protected int loaderId() {
        return 5;
    }
}
