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

import com.tokenbrowser.presenter.FullscreenImagePresenter;
import com.tokenbrowser.presenter.factory.FullscreenImagePresenterFactory;
import com.tokenbrowser.presenter.factory.PresenterFactory;
import com.tokenbrowser.R;
import com.tokenbrowser.databinding.ActivityFullscreenImageBinding;

public class FullscreenImageActivity extends BasePresenterActivity<FullscreenImagePresenter, FullscreenImageActivity> {

    public static final String FILE_PATH = "file_path";
    private ActivityFullscreenImageBinding binding;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    private void init() {
        this.binding = DataBindingUtil.setContentView(this, R.layout.activity_fullscreen_image);
    }

    public final ActivityFullscreenImageBinding getBinding() {
        return this.binding;
    }

    @NonNull
    @Override
    protected PresenterFactory<FullscreenImagePresenter> getPresenterFactory() {
        return new FullscreenImagePresenterFactory();
    }

    @Override
    protected void onPresenterPrepared(@NonNull FullscreenImagePresenter presenter) {}

    @Override
    protected int loaderId() {
        return 4004;
    }
}
