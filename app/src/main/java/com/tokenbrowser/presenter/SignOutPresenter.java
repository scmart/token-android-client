package com.tokenbrowser.presenter;

import android.content.Intent;

import com.tokenbrowser.view.BaseApplication;
import com.tokenbrowser.view.activity.SignInActivity;
import com.tokenbrowser.view.activity.SignOutActivity;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class SignOutPresenter implements Presenter<SignOutActivity> {

    private SignOutActivity activity;
    private Subscription clearDataSubscription;

    @Override
    public void onViewAttached(SignOutActivity view) {
        this.activity = view;
        clearUserDataAndLogOut();
    }

    private void clearUserDataAndLogOut() {
        this.clearDataSubscription = BaseApplication
                .get()
                .getTokenManager()
                .clearUserData()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::goToSignInActivity);
    }

    private void goToSignInActivity() {
        final Intent intent = new Intent(this.activity, SignInActivity.class);
        this.activity.startActivity(intent);
        this.activity.finish();
    }

    @Override
    public void onViewDetached() {
        if (this.clearDataSubscription != null) {
            this.clearDataSubscription.unsubscribe();
        }
        this.activity = null;
    }

    @Override
    public void onDestroyed() {
        this.clearDataSubscription = null;
        this.activity = null;
    }
}
