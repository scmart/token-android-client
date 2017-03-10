package com.tokenbrowser.presenter;

import android.view.View;

import com.tokenbrowser.model.local.Library;
import com.tokenbrowser.view.activity.LicenseActivity;

public class LicensePresenter implements Presenter<LicenseActivity> {

    private LicenseActivity activity;
    private Library library;

    @Override
    public void onViewAttached(LicenseActivity view) {
        this.activity = view;
        getIntentData();
        initView();
        initClickListeners();
    }

    private void getIntentData() {
        this.library = this.activity.getIntent().getParcelableExtra(LicenseActivity.LIBRARY);
    }

    private void initView() {
        this.activity.getBinding().title.setText(library.getName());
        this.activity.getBinding().license.setText(library.getLicence());
    }

    private void initClickListeners() {
        this.activity.getBinding().closeButton.setOnClickListener(this::handleCloseButtonClicked);
    }

    private void handleCloseButtonClicked(final View v) {
        this.activity.finish();
    }

    @Override
    public void onViewDetached() {
        this.activity = null;
    }

    @Override
    public void onDestroyed() {}
}
