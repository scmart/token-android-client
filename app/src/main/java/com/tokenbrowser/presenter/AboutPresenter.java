/*
 * 	Copyright (c) 2017. Token Browser, Inc
 *
 * 	This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.tokenbrowser.presenter;

import android.content.Intent;
import android.view.View;

import com.tokenbrowser.BuildConfig;
import com.tokenbrowser.R;
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
    public void onDestroyed() {}
}
