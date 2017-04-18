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

package com.tokenbrowser.view.activity;


import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;

import com.tokenbrowser.R;
import com.tokenbrowser.databinding.ActivityMainBinding;
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
    private MainPresenter presenter;

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
        this.presenter = presenter;
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

    @Override
    public void onRestoreInstanceState(final Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        this.presenter.onRestoreInstanceState(savedInstanceState);
    }
}
