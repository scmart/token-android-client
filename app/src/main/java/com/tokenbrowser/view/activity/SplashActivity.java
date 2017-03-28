package com.tokenbrowser.view.activity;


import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.tokenbrowser.BuildConfig;
import com.tokenbrowser.crypto.HDWallet;
import com.tokenbrowser.view.BaseApplication;
import com.crashlytics.android.Crashlytics;

import io.fabric.sdk.android.Fabric;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!BuildConfig.DEBUG) {
            Fabric.with(this, new Crashlytics());
        }

        BaseApplication
                .get()
                .getTokenManager()
                .getWallet()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::gotoMainActivity);
    }

    private void gotoMainActivity(final HDWallet unused) {
        final Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
