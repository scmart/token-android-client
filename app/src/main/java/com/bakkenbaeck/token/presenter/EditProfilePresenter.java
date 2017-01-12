package com.bakkenbaeck.token.presenter;


import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Toast;

import com.bakkenbaeck.token.R;
import com.bakkenbaeck.token.model.User;
import com.bakkenbaeck.token.network.rest.model.UserDetails;
import com.bakkenbaeck.token.util.OnNextSubscriber;
import com.bakkenbaeck.token.util.OnSingleClickListener;
import com.bakkenbaeck.token.view.BaseApplication;
import com.bakkenbaeck.token.view.activity.ProfileActivity;
import com.bakkenbaeck.token.view.fragment.children.EditProfileFragment;

import rx.SingleSubscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class EditProfilePresenter implements Presenter<EditProfileFragment> {

    private OnNextSubscriber<User> handleUserLoaded;
    private EditProfileFragment fragment;

    private String nameFieldContents;
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
        parentActivity.getBinding().closeButton.setImageDrawable(parentActivity.getResources().getDrawable(R.drawable.ic_arrow_back));
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
                if(nameFieldContents == null) nameFieldContents = user.getUsername();
                if(aboutFieldContents == null) aboutFieldContents = user.getAbout();
                if(locationFieldContents == null) locationFieldContents = user.getLocation();
            }
        };
    }

    private void updateView() {
        this.fragment.getBinding().inputName.setText(this.nameFieldContents);
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
                    .setUsername(fragment.getBinding().inputName.getText().toString())
                    .setAbout(fragment.getBinding().inputAbout.getText().toString())
                    .setLocation(fragment.getBinding().inputLocation.getText().toString());

            BaseApplication.get()
                    .getTokenManager()
                    .getUserManager()
                    .updateUser(userDetails, handleUserUpdated);
        }

        private boolean validate() {
            final String username = fragment.getBinding().inputName.getText().toString().trim();
            if (username.length() == 0) {
                fragment.getBinding().inputName.setError(fragment.getResources().getString(R.string.error__required));
                fragment.getBinding().inputName.requestFocus();
                return false;
            }

            if (username.contains(" ")) {
                fragment.getBinding().inputName.setError(fragment.getResources().getString(R.string.error__invalid_characters));
                fragment.getBinding().inputName.requestFocus();
                return false;
            }
            return true;
        }
    };

    private final SingleSubscriber<Void> handleUserUpdated = new SingleSubscriber<Void>() {
        @Override
        public void onSuccess(final Void unused) {
            showToast("Saved successfully!");
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    fragment.getActivity().onBackPressed();
                }
            });
        }

        @Override
        public void onError(final Throwable error) {
            showToast("Error updating profile. Try a different username");
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
        saveFields();
        this.fragment = null;
        this.handleUserLoaded.unsubscribe();
    }

    private void saveFields() {
        this.nameFieldContents = this.fragment.getBinding().inputName.getText().toString();
        this.aboutFieldContents = this.fragment.getBinding().inputAbout.getText().toString();
        this.locationFieldContents = this.fragment.getBinding().inputLocation.getText().toString();
    }

    @Override
    public void onViewDestroyed() {
        this.fragment = null;
    }
}
