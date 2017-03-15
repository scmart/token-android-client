package com.tokenbrowser.view.activity;


import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;

import com.tokenbrowser.token.R;
import com.tokenbrowser.token.databinding.ActivityMainBinding;
import com.tokenbrowser.presenter.MainPresenter;
import com.tokenbrowser.presenter.factory.MainPresenterFactory;
import com.tokenbrowser.presenter.factory.PresenterFactory;
import com.tokenbrowser.service.RegistrationIntentService;
import com.tokenbrowser.view.custom.OfflineViewRenderer;

public class MainActivity
        extends OfflineViewBasePresenterActivity<MainPresenter, MainActivity>
        implements OfflineViewRenderer {

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

    @Override
    public View getOfflineViewContainer() {
        return this.binding.snackbarContainer;
    }
}
