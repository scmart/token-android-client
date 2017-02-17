package com.bakkenbaeck.token.presenter;

import com.bakkenbaeck.token.model.local.Library;
import com.bakkenbaeck.token.view.activity.LicenseActivity;

public class LicensePresenter implements Presenter<LicenseActivity> {

    private LicenseActivity activity;

    @Override
    public void onViewAttached(LicenseActivity view) {
        this.activity = view;
        getIntentData();
    }

    private void getIntentData() {
        final Library library = this.activity.getIntent().getParcelableExtra(LicenseActivity.LIBRARY);
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
