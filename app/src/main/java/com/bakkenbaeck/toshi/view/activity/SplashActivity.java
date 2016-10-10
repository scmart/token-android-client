package com.bakkenbaeck.toshi.view.activity;


import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.bakkenbaeck.toshi.R;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_splash);

        final Intent intent = new Intent(this, ChatActivity.class);
        startActivity(intent);
        finish();
    }
}
