package com.bakkenbaeck.token.presenter;


import com.bakkenbaeck.token.R;
import com.bakkenbaeck.token.model.User;
import com.bakkenbaeck.token.util.SingleSuccessSubscriber;
import com.bakkenbaeck.token.view.BaseApplication;
import com.bakkenbaeck.token.view.activity.ProfileActivity;
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
