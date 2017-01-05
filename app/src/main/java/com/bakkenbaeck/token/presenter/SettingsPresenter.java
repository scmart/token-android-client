package com.bakkenbaeck.token.presenter;

import android.content.Intent;
import android.view.View;

import com.bakkenbaeck.token.model.User;
import com.bakkenbaeck.token.util.OnSingleClickListener;
import com.bakkenbaeck.token.util.SingleSuccessSubscriber;
import com.bakkenbaeck.token.view.BaseApplication;
import com.bakkenbaeck.token.view.activity.ProfileActivity;
import com.bakkenbaeck.token.view.fragment.children.SettingsFragment;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public final class SettingsPresenter implements
        Presenter<SettingsFragment> {

    private boolean firstTimeAttaching = true;
    private User localUser;
    private SettingsFragment fragment;

    @Override
    public void onViewAttached(final SettingsFragment fragment) {
        this.fragment = fragment;
        if (this.firstTimeAttaching) {
            this.firstTimeAttaching = false;
            init();
        }

        updateUi();
    }

    private void init() {
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
            SettingsPresenter.this.localUser = user;
            updateUi();
            this.unsubscribe();
        }
    };

    private void updateUi() {
        if (this.localUser != null) {
            this.fragment.getBinding().name.setText(this.localUser.getUsername());
        }

        this.fragment.getBinding().myProfileCard.setOnClickListener(this.handleMyProfileClicked);
    }

    private final OnSingleClickListener handleMyProfileClicked = new OnSingleClickListener() {
        @Override
        public void onSingleClick(final View v) {
            final Intent intent = new Intent(fragment.getActivity(), ProfileActivity.class);
            fragment.startActivity(intent);
        }
    };

    @Override
    public void onViewDetached() {}

    @Override
    public void onViewDestroyed() {
        destroy();
    }

    private void destroy() {
        this.handleUserLoaded.unsubscribe();
        this.fragment = null;
    }
}
