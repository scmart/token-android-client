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

import com.tokenbrowser.R;
import com.tokenbrowser.databinding.ActivityViewAppBinding;
import com.tokenbrowser.model.local.ActivityResultHolder;
import com.tokenbrowser.presenter.ViewAppPresenter;
import com.tokenbrowser.presenter.factory.PresenterFactory;
import com.tokenbrowser.presenter.factory.ViewAppPresenterFactory;

public class ViewAppActivity extends BasePresenterActivity<ViewAppPresenter, ViewAppActivity> {

    public static final String APP_OWNER_ADDRESS = "app_owner_address";

    private ActivityViewAppBinding binding;
    private ViewAppPresenter presenter;
    private ActivityResultHolder resultHolder;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    private void init() {
        this.binding = DataBindingUtil.setContentView(this, R.layout.activity_view_app);
    }

    @Override
    public void onResume() {
        super.onResume();
        tryProcessResultHolder();
    }

    public ActivityViewAppBinding getBinding() {
        return this.binding;
    }


    @NonNull
    @Override
    protected PresenterFactory<ViewAppPresenter> getPresenterFactory() {
        return new ViewAppPresenterFactory();
    }

    @Override
    protected void onPresenterPrepared(@NonNull final ViewAppPresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        this.resultHolder = new ActivityResultHolder(requestCode, resultCode, data);
        tryProcessResultHolder();
    }

    private void tryProcessResultHolder() {
        if (this.presenter == null || this.resultHolder == null) return;
        if (this.presenter.handleActivityResult(this.resultHolder)) {
            this.resultHolder = null;
        }
    }

    @Override
    protected int loaderId() {
        return 2001;
    }
}
