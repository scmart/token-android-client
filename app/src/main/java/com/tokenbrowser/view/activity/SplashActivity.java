package com.tokenbrowser.view.activity;

import android.os.Bundle;
import android.support.annotation.NonNull;

import com.tokenbrowser.BuildConfig;
import com.crashlytics.android.Crashlytics;
import com.tokenbrowser.presenter.SplashPresenter;
import com.tokenbrowser.presenter.factory.PresenterFactory;
import com.tokenbrowser.presenter.factory.SplashPresenterFactory;

import io.fabric.sdk.android.Fabric;

public class SplashActivity extends BasePresenterActivity<SplashPresenter, SplashActivity> {

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!BuildConfig.DEBUG) {
            Fabric.with(this, new Crashlytics());
        }
    }

    @NonNull
    @Override
    protected PresenterFactory<SplashPresenter> getPresenterFactory() {
        return new SplashPresenterFactory();
    }

    @Override
    protected void onPresenterPrepared(@NonNull SplashPresenter presenter) {}

    @Override
    protected int loaderId() {
        return 1;
    }
}
