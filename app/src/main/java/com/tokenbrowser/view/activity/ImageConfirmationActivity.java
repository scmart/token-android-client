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

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.tokenbrowser.presenter.ImageConfirmationPresenter;
import com.tokenbrowser.presenter.factory.ImageConfirmationPresenterFactory;
import com.tokenbrowser.presenter.factory.PresenterFactory;
import com.tokenbrowser.R;
import com.tokenbrowser.databinding.ActivityImageConfirmationBinding;

public class ImageConfirmationActivity extends BasePresenterActivity<ImageConfirmationPresenter, ImageConfirmationActivity> {

    public static final String FILE_URI = "file_uri";
    public static final String FILE_PATH = "file_path";

    private ActivityImageConfirmationBinding binding;
    private ImageConfirmationPresenter presenter;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    private void init() {
        this.binding = DataBindingUtil.setContentView(this, R.layout.activity_image_confirmation);
    }

    public ActivityImageConfirmationBinding getBinding() {
        return this.binding;
    }

    @NonNull
    @Override
    protected PresenterFactory<ImageConfirmationPresenter> getPresenterFactory() {
        return new ImageConfirmationPresenterFactory();
    }

    @Override
    protected void onPresenterPrepared(@NonNull ImageConfirmationPresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    protected void onPresenterDestroyed() {
        this.presenter = null;
    }

    @Override
    protected int loaderId() {
        return 4007;
    }

    @Override
    public void onBackPressed() {
        if (this.presenter == null) {
            super.onBackPressed();
            return;
        }

        this.presenter.handleBackButtonClicked();
    }
}
