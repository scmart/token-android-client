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
