package com.tokenbrowser.view.fragment.children;


import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tokenbrowser.model.local.ActivityResultHolder;
import com.tokenbrowser.model.local.PermissionResultHolder;
import com.tokenbrowser.R;
import com.tokenbrowser.databinding.FragmentEditProfileBinding;
import com.tokenbrowser.presenter.EditProfilePresenter;
import com.tokenbrowser.presenter.factory.EditProfilePresenterFactory;
import com.tokenbrowser.presenter.factory.PresenterFactory;
import com.tokenbrowser.view.fragment.BasePresenterFragment;

public class EditProfileFragment extends BasePresenterFragment<EditProfilePresenter, EditProfileFragment> {

    private FragmentEditProfileBinding binding;
    private EditProfilePresenter presenter;
    private ActivityResultHolder resultHolder;
    private PermissionResultHolder permissionResultHolder;

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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        this.resultHolder = new ActivityResultHolder(requestCode, resultCode, data);
        tryProcessResultHolder();
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode,
                                           @NonNull final String permissions[],
                                           @NonNull final int[] grantResults) {
        this.permissionResultHolder = new PermissionResultHolder(requestCode, permissions, grantResults);
        tryProcessPermissionResultHolder();
    }

    private void tryProcessResultHolder() {
        if (this.presenter == null || this.resultHolder == null) return;

        if (this.presenter.handleActivityResult(this.resultHolder)) {
            this.resultHolder = null;
        }
    }

    private void tryProcessPermissionResultHolder() {
        if (this.presenter == null || this.permissionResultHolder == null) return;

        if (this.presenter.handlePermissionResult(this.permissionResultHolder)) {
            this.permissionResultHolder = null;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        tryProcessResultHolder();
        tryProcessPermissionResultHolder();
    }

    @Override
    protected void onPresenterPrepared(@NonNull final EditProfilePresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    protected void onPresenterDestroyed() {
        super.onPresenterDestroyed();
        this.presenter = null;
    }

    @Override
    protected int loaderId() {
        return 5003;
    }
}
