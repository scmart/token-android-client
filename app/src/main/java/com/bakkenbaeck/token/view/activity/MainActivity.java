package com.bakkenbaeck.token.view.activity;


import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.bakkenbaeck.token.R;
import com.bakkenbaeck.token.databinding.ActivityMainBinding;
import com.bakkenbaeck.token.presenter.MainPresenter;
import com.bakkenbaeck.token.presenter.factory.MainPresenterFactory;
import com.bakkenbaeck.token.presenter.factory.PresenterFactory;
import com.bakkenbaeck.token.service.RegistrationIntentService;

public class MainActivity extends BasePresenterActivity<MainPresenter, MainActivity> {

    public static final String EXTRA__ACTIVE_TAB = "active_tab";
    private static final int UNIQUE_ACTIVITY_ID = 9000;
    private ActivityMainBinding binding;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
        startGcmRegistration();
    }

    private void init() {
        this.binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
    }

    @NonNull
    @Override
    protected PresenterFactory<MainPresenter> getPresenterFactory() {
        return new MainPresenterFactory();
    }

    @Override
    public int loaderId() {
        return UNIQUE_ACTIVITY_ID;
    }

    @Override
    protected void onPresenterPrepared(@NonNull final MainPresenter presenter) {
        // Nothing to do
    }

    public final ActivityMainBinding getBinding() {
        return this.binding;
    }

    private void startGcmRegistration() {
        final Intent intent = new Intent(this, RegistrationIntentService.class);
        startService(intent);
    }
}
