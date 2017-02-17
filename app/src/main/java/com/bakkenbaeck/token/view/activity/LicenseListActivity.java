package com.bakkenbaeck.token.view.activity;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.bakkenbaeck.token.R;
import com.bakkenbaeck.token.databinding.ActivityLibLicensesBinding;
import com.bakkenbaeck.token.presenter.LicenseListPresenter;
import com.bakkenbaeck.token.presenter.factory.LicenseListPresenterFactory;
import com.bakkenbaeck.token.presenter.factory.PresenterFactory;

public class LicenseListActivity extends BasePresenterActivity<LicenseListPresenter, LicenseListActivity> {

    private ActivityLibLicensesBinding binding;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    private void init() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_lib_licenses);
    }

    public final ActivityLibLicensesBinding getBinding() {
        return this.binding;
    }

    @NonNull
    @Override
    protected PresenterFactory<LicenseListPresenter> getPresenterFactory() {
        return new LicenseListPresenterFactory();
    }

    @Override
    protected void onPresenterPrepared(@NonNull LicenseListPresenter presenter) {}

    @Override
    protected int loaderId() {
        return 5002;
    }
}
