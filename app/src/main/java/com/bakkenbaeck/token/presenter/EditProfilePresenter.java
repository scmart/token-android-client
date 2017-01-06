package com.bakkenbaeck.token.presenter;


import com.bakkenbaeck.token.model.User;
import com.bakkenbaeck.token.util.SingleSuccessSubscriber;
import com.bakkenbaeck.token.view.BaseApplication;
import com.bakkenbaeck.token.view.fragment.children.EditProfileFragment;

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

        updateView();
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

    @Override
    public void onViewDetached() {
        this.fragment = null;
    }

    @Override
    public void onViewDestroyed() {
        this.fragment = null;
    }
}
