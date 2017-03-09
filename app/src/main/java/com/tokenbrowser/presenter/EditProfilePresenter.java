package com.tokenbrowser.presenter;


import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Toast;

import com.tokenbrowser.token.R;
import com.tokenbrowser.model.local.User;
import com.tokenbrowser.model.network.UserDetails;
import com.tokenbrowser.util.OnNextSubscriber;
import com.tokenbrowser.util.OnSingleClickListener;
import com.tokenbrowser.view.BaseApplication;
import com.tokenbrowser.view.activity.ProfileActivity;
import com.tokenbrowser.view.fragment.children.EditProfileFragment;

import rx.SingleSubscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class EditProfilePresenter implements Presenter<EditProfileFragment> {

    private OnNextSubscriber<User> handleUserLoaded;
    private EditProfileFragment fragment;

    private String displayNameFieldContents;
    private String userNameFieldContents;
    private String aboutFieldContents;
    private String locationFieldContents;

    @Override
    public void onViewAttached(final EditProfileFragment fragment) {
        this.fragment = fragment;
        initShortLivingObjects();
    }

    private void initShortLivingObjects() {
        initToolbar();
        updateView();
        attachListeners();
    }

    private void initToolbar() {
        final ProfileActivity parentActivity = (ProfileActivity) this.fragment.getActivity();
        parentActivity.getBinding().title.setText(R.string.edit_profile);
        parentActivity.getBinding().closeButton.setImageResource(R.drawable.ic_arrow_back);
        parentActivity.getBinding().saveButton.setVisibility(View.VISIBLE);
        parentActivity.getBinding().saveButton.setOnClickListener(this.handleSaveClicked);
    }

    private void attachListeners() {
        generateUserLoadedHandler();
        BaseApplication.get()
                .getTokenManager()
                .getUserManager()
                .getUserObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this.handleUserLoaded);
    }

    private void generateUserLoadedHandler() {
        this.handleUserLoaded = new OnNextSubscriber<User>() {
            @Override
            public void onNext(final User user) {
                setFieldsFromUser(user);
                updateView();
            }

            private void setFieldsFromUser(final User user) {
                if(displayNameFieldContents == null) displayNameFieldContents = user.getDisplayName();
                if(userNameFieldContents == null) userNameFieldContents = user.getUsernameForEditing();
                if(aboutFieldContents == null) aboutFieldContents = user.getAbout();
                if(locationFieldContents == null) locationFieldContents = user.getLocation();
            }
        };
    }

    private void updateView() {
        this.fragment.getBinding().inputName.setText(this.displayNameFieldContents);
        this.fragment.getBinding().inputUsername.setText(this.userNameFieldContents);
        this.fragment.getBinding().inputAbout.setText(this.aboutFieldContents);
        this.fragment.getBinding().inputLocation.setText(this.locationFieldContents);
    }

    private final OnSingleClickListener handleSaveClicked = new OnSingleClickListener() {
        @Override
        public void onSingleClick(final View v) {
            if (!validate()) {
                return;
            }
            final UserDetails userDetails =
                    new UserDetails()
                    .setDisplayName(fragment.getBinding().inputName.getText().toString().trim())
                    .setUsername(fragment.getBinding().inputUsername.getText().toString().trim())
                    .setAbout(fragment.getBinding().inputAbout.getText().toString().trim())
                    .setLocation(fragment.getBinding().inputLocation.getText().toString().trim());

            BaseApplication.get()
                    .getTokenManager()
                    .getUserManager()
                    .updateUser(userDetails, handleUserUpdated);
        }

        private boolean validate() {
            final String displayName = fragment.getBinding().inputName.getText().toString().trim();
            final String username = fragment.getBinding().inputUsername.getText().toString().trim();

            if (displayName.trim().length() == 0) {
                fragment.getBinding().inputName.setError(fragment.getResources().getString(R.string.error__required));
                fragment.getBinding().inputName.requestFocus();
                return false;
            }

            if (username.trim().length() == 0) {
                fragment.getBinding().inputUsername.setError(fragment.getResources().getString(R.string.error__required));
                fragment.getBinding().inputUsername.requestFocus();
                return false;
            }

            if (username.contains(" ")) {
                fragment.getBinding().inputUsername.setError(fragment.getResources().getString(R.string.error__invalid_characters));
                fragment.getBinding().inputUsername.requestFocus();
                return false;
            }
            return true;
        }
    };

    private final SingleSubscriber<User> handleUserUpdated = new SingleSubscriber<User>() {
        @Override
        public void onSuccess(final User unused) {
            showToast("Saved successfully!");
            new Handler(Looper.getMainLooper()).post(() -> fragment.getActivity().onBackPressed());
        }

        @Override
        public void onError(final Throwable error) {
            showToast("Error updating profile. Try a different username");
        }
    };

    private void showToast(final String message) {
        new Handler(Looper.getMainLooper())
                .post(() -> Toast.makeText(fragment.getContext(), message, Toast.LENGTH_LONG).show());
    }

    @Override
    public void onViewDetached() {
        saveFields();
        this.fragment = null;
        this.handleUserLoaded.unsubscribe();
    }

    private void saveFields() {
        this.displayNameFieldContents = this.fragment.getBinding().inputName.getText().toString();
        this.userNameFieldContents = this.fragment.getBinding().inputUsername.getText().toString();
        this.aboutFieldContents = this.fragment.getBinding().inputAbout.getText().toString();
        this.locationFieldContents = this.fragment.getBinding().inputLocation.getText().toString();
    }

    @Override
    public void onViewDestroyed() {
        this.fragment = null;
    }
}
