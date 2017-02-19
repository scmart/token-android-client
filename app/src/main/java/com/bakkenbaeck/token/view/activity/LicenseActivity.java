package com.bakkenbaeck.token.view.activity;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.bakkenbaeck.token.R;
import com.bakkenbaeck.token.databinding.ActivityLicenseBinding;
import com.bakkenbaeck.token.presenter.LicensePresenter;
import com.bakkenbaeck.token.presenter.factory.LicensePresenterFactory;
import com.bakkenbaeck.token.presenter.factory.PresenterFactory;

public class LicenseActivity extends BasePresenterActivity<LicensePresenter, LicenseActivity> {

    public static final String LIBRARY = "library";

    private ActivityLicenseBinding binding;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    private void init() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_license);
    }

    public ActivityLicenseBinding getBinding() {
        return this.binding;
    }

    @NonNull
    @Override
    protected PresenterFactory<LicensePresenter> getPresenterFactory() {
        return new LicensePresenterFactory();
    }

    @Override
    protected void onPresenterPrepared(@NonNull LicensePresenter presenter) {}

    @Override
    protected int loaderId() {
        return 5003;
    }
}
