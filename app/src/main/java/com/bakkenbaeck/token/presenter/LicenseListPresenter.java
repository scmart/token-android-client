package com.bakkenbaeck.token.presenter;

import com.bakkenbaeck.token.view.activity.LicenseListActivity;

public class LicenseListPresenter implements Presenter<LicenseListActivity> {
    
    private LicenseListActivity activity;
    
    @Override
    public void onViewAttached(LicenseListActivity view) {
        this.activity = view;
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
