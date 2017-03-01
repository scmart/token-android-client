package com.tokenbrowser.presenter;

import android.content.Intent;
import android.view.View;

import com.tokenbrowser.token.BuildConfig;
import com.tokenbrowser.token.R;
import com.tokenbrowser.view.activity.AboutActivity;
import com.tokenbrowser.view.activity.LicenseListActivity;

public class AboutPresenter implements Presenter<AboutActivity> {

    private AboutActivity activity;

    @Override
    public void onViewAttached(AboutActivity view) {
        this.activity = view;
        initView();
    }

    private void initView() {
        setVersionName();
        setContactInfo();
        initClickListeners();
    }

    private void setVersionName() {
        final String versionName = BuildConfig.VERSION_NAME;
        this.activity.getBinding().version.setText(versionName);
    }

    private void setContactInfo() {
        final String contactInfo = this.activity.getString(R.string.contact_information);
        this.activity.getBinding().contactInfo.setText(contactInfo);
    }

    private void initClickListeners() {
        this.activity.getBinding().closeButton.setOnClickListener(this::handleCloseButtonClicked);
        this.activity.getBinding().openSourceLicenses.setOnClickListener(this::handleOpenSourceLicencesClicked);
    }

    private void handleCloseButtonClicked(final View v) {
        this.activity.finish();
    }

    private void handleOpenSourceLicencesClicked(final View v) {
        final Intent intent = new Intent(this.activity, LicenseListActivity.class);
        this.activity.startActivity(intent);
    }

    @Override
    public void onViewDetached() {
        this.activity = null;
    }

    @Override
    public void onViewDestroyed() {
        this.activity = null;
    }
}
