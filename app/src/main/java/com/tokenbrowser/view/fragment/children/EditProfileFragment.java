package com.tokenbrowser.view.fragment.children;


import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tokenbrowser.token.R;
import com.tokenbrowser.token.databinding.FragmentEditProfileBinding;
import com.tokenbrowser.presenter.EditProfilePresenter;
import com.tokenbrowser.presenter.factory.EditProfilePresenterFactory;
import com.tokenbrowser.presenter.factory.PresenterFactory;
import com.tokenbrowser.view.fragment.BasePresenterFragment;

public class EditProfileFragment extends BasePresenterFragment<EditProfilePresenter, EditProfileFragment> {

    private FragmentEditProfileBinding binding;

    public static EditProfileFragment newInstance() {
        return new EditProfileFragment();
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater,
                             final ViewGroup container,
                             final Bundle inState) {
        this.binding = DataBindingUtil.inflate(inflater, R.layout.fragment_edit_profile, container, false);
        return binding.getRoot();
    }

    public FragmentEditProfileBinding getBinding() {
        return this.binding;
    }

    @NonNull
    @Override
    protected PresenterFactory<EditProfilePresenter> getPresenterFactory() {
        return new EditProfilePresenterFactory();
    }

    @Override
    protected void onPresenterPrepared(@NonNull final EditProfilePresenter presenter) {}

    @Override
    protected int loaderId() {
        return 5003;
    }
}
