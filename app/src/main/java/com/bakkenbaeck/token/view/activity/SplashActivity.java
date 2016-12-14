package com.bakkenbaeck.token.view.activity;


import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.bakkenbaeck.token.presenter.MainPresenter;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
