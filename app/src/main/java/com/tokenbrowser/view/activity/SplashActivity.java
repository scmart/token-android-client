package com.tokenbrowser.view.activity;


import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.tokenbrowser.BuildConfig;
import com.crashlytics.android.Crashlytics;
import com.tokenbrowser.util.SharedPrefsUtil;
import com.tokenbrowser.view.BaseApplication;

import io.fabric.sdk.android.Fabric;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class SplashActivity extends AppCompatActivity {

    private CompositeSubscription subscriptions;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!BuildConfig.DEBUG) {
            Fabric.with(this, new Crashlytics());
        }

        this.subscriptions = new CompositeSubscription();
        redirect();
    }

    private void redirect() {
        final boolean hasSignedOut = SharedPrefsUtil.hasSignedOut();

        if (!hasSignedOut) {
            initManagersAndGoToMainActivity();
        } else {
            goToSignInActivity();
        }
    }

    private void initManagersAndGoToMainActivity() {
        final Subscription sub = BaseApplication
                .get()
                .getTokenManager()
                .init()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(tokenManager -> goToMainActivity());

        this.subscriptions.add(sub);
    }

    private void goToMainActivity() {
        SharedPrefsUtil.setSignedIn();
        final Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void goToSignInActivity() {
        final Intent intent = new Intent(this, SignInActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.subscriptions.clear();
        this.subscriptions = null;
    }
}
