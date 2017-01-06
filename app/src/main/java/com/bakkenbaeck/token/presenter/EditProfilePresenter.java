package com.bakkenbaeck.token.presenter;


import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Toast;

import com.bakkenbaeck.token.R;
import com.bakkenbaeck.token.model.User;
import com.bakkenbaeck.token.network.rest.model.UserDetails;
import com.bakkenbaeck.token.util.OnSingleClickListener;
import com.bakkenbaeck.token.util.SingleSuccessSubscriber;
import com.bakkenbaeck.token.view.BaseApplication;
import com.bakkenbaeck.token.view.activity.ProfileActivity;
import com.bakkenbaeck.token.view.fragment.children.EditProfileFragment;

import rx.SingleSubscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class EditProfilePresenter implements Presenter<EditProfileFragment> {

    private EditProfileFragment fragment;
    private User localUser;
    private boolean firstTimeAttached = true;

    @Override
    public void onViewAttached(final EditProfileFragment fragment) {
        this.fragment = fragment;
        if (this.firstTimeAttached) {
            this.firstTimeAttached = false;
            initLongLivingObjects();
        }

        initShortLivingObjects();
    }

    private void initShortLivingObjects() {
        initToolbar();
        updateView();
    }

    private void initToolbar() {
        final ProfileActivity parentActivity = (ProfileActivity) this.fragment.getActivity();
        parentActivity.getBinding().title.setText(R.string.edit_profile);
        parentActivity.getBinding().closeButton.setImageDrawable(parentActivity.getResources().getDrawable(R.drawable.ic_arrow_back));
        parentActivity.getBinding().saveButton.setVisibility(View.VISIBLE);
        parentActivity.getBinding().saveButton.setOnClickListener(this.handleSaveClicked);
    }

    private void initLongLivingObjects() {
        BaseApplication.get()
                .getTokenManager()
                .getUserManager()
                .getUser()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this.handleUserLoaded);
    }

    private final SingleSuccessSubscriber<User> handleUserLoaded = new SingleSuccessSubscriber<User>() {
        @Override
        public void onSuccess(final User user) {
            EditProfilePresenter.this.localUser = user;
            updateView();
            this.unsubscribe();
        }
    };

    private void updateView() {
        if (this.localUser == null) {
            return;
        }

        this.fragment.getBinding().inputName.setText(this.localUser.getUsername());
    }

    private final OnSingleClickListener handleSaveClicked = new OnSingleClickListener() {
        @Override
        public void onSingleClick(final View v) {
            final UserDetails userDetails =
                    new UserDetails()
                    .setUsername(fragment.getBinding().inputName.getText().toString())
                    .setAbout(fragment.getBinding().inputAbout.getText().toString())
                    .setLocation(fragment.getBinding().inputLocation.getText().toString());

            BaseApplication.get()
                    .getTokenManager()
                    .getUserManager()
                    .updateUser(userDetails, handleUserUpdated);
        }
    };

    private final SingleSubscriber<Void> handleUserUpdated = new SingleSubscriber<Void>() {
        @Override
        public void onSuccess(final Void unused) {
            showToast("Saved successfully!");
        }

        @Override
        public void onError(final Throwable error) {
            showToast("Error updating profile.");
        }
    };

    private void showToast(final String message) {
        new Handler(Looper.getMainLooper())
                .post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(fragment.getContext(), message, Toast.LENGTH_LONG).show();
                    }
                });
    }

    @Override
    public void onViewDetached() {
        this.fragment = null;
    }

    @Override
    public void onViewDestroyed() {
        this.fragment = null;
    }
}
