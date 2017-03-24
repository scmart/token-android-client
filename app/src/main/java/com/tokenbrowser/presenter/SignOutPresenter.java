package com.tokenbrowser.presenter;

import android.content.Intent;

import com.tokenbrowser.view.BaseApplication;
import com.tokenbrowser.view.activity.SignInActivity;
import com.tokenbrowser.view.activity.SignOutActivity;

import rx.Completable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class SignOutPresenter implements Presenter<SignOutActivity> {

    private SignOutActivity activity;
    private Subscription signOutSubscription;

    @Override
    public void onViewAttached(SignOutActivity view) {
        this.activity = view;
        clearTasks();
    }

    private void clearTasks() {
        this.signOutSubscription = unregisterGcm()
                .andThen(clearUserDataAndLogOut())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::goToSignInActivity);
    }

    private Completable clearUserDataAndLogOut() {
        return BaseApplication
                .get()
                .getTokenManager()
                .clearUserData();
    }

    private Completable unregisterGcm() {
        return BaseApplication
                .get()
                .getTokenManager()
                .getSofaMessageManager()
                .tryUnregisterGcm();
    }

    private void goToSignInActivity() {
        final Intent intent = new Intent(this.activity, SignInActivity.class);
        this.activity.startActivity(intent);
        this.activity.finish();
    }

    @Override
    public void onViewDetached() {
        if (this.signOutSubscription != null) {
            this.signOutSubscription.unsubscribe();
        }
        this.activity = null;
    }

    @Override
    public void onDestroyed() {
        this.signOutSubscription = null;
        this.activity = null;
    }
}
