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
    private User localUser;

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
                EditProfilePresenter.this.localUser = user;
                updateView();
            }
        };
    }

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
        this.handleUserLoaded.unsubscribe();
    }

    @Override
    public void onViewDestroyed() {
        this.fragment = null;
    }
}
