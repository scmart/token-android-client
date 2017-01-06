package com.bakkenbaeck.token.presenter;

import android.support.v4.app.FragmentTransaction;
import android.view.View;

import com.bakkenbaeck.token.R;
import com.bakkenbaeck.token.util.OnSingleClickListener;
import com.bakkenbaeck.token.view.activity.ProfileActivity;
import com.bakkenbaeck.token.view.fragment.children.EditProfileFragment;
import com.bakkenbaeck.token.view.fragment.children.ViewProfileFragment;


public final class ProfilePresenter implements Presenter<ProfileActivity> {

    private ProfileActivity activity;

    public interface OnEditButtonListener {
        void onClick();
    }

    private boolean firstTimeAttaching = true;

    @Override
    public void onViewAttached(final ProfileActivity activity) {
        this.activity = activity;
        if (this.firstTimeAttaching) {
            this.firstTimeAttaching = false;
            manuallyAddRootFragment();
        }

        initToolbar();
    }

    private void manuallyAddRootFragment() {
        final ViewProfileFragment rootFragment = ViewProfileFragment.newInstance();
        rootFragment.setOnEditButtonListener(this.onEditButtonListener);

        final FragmentTransaction transaction = this.activity.getSupportFragmentManager().beginTransaction();
        transaction.replace(this.activity.getBinding().container.getId(), rootFragment).commit();
    }

    private final OnEditButtonListener onEditButtonListener = new OnEditButtonListener() {
        @Override
        public void onClick() {
            final int fragmentContainerId = activity.getBinding().container.getId();
            final EditProfileFragment editProfileFragment = EditProfileFragment.newInstance();

            activity.getSupportFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left, R.anim.slide_out_right)
                    .replace(fragmentContainerId, editProfileFragment)
                    .addToBackStack(String.valueOf(fragmentContainerId))
                    .commit();
        }
    };

    private void initToolbar() {
        this.activity.getBinding().closeButton.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(final View v) {
                activity.onBackPressed();
            }
        });
    }

    @Override
    public void onViewDetached() {

    }

    @Override
    public void onViewDestroyed() {
    }
}
