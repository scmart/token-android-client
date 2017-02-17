package com.bakkenbaeck.token.presenter;

import com.bakkenbaeck.token.BuildConfig;
import com.bakkenbaeck.token.R;
import com.bakkenbaeck.token.view.activity.AboutActivity;

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
    }

    private void setVersionName() {
        final String versionName = BuildConfig.VERSION_NAME;
        this.activity.getBinding().version.setText(versionName);
    }

    private void setContactInfo() {
        final String contactInfo = this.activity.getString(R.string.contact_information);
        this.activity.getBinding().contactInfo.setText(contactInfo);
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
