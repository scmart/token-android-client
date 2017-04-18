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

import android.view.WindowManager;

import com.tokenbrowser.util.ImageUtil;
import com.tokenbrowser.view.activity.FullscreenImageActivity;

import java.io.File;

public class FullscreenImagePresenter implements Presenter<FullscreenImageActivity> {

    private FullscreenImageActivity activity;
    private String filePath;

    @Override
    public void onViewAttached(FullscreenImageActivity view) {
        this.activity = view;
        getIntentData();
        hideStatusBar();
        initView();
    }

    private void getIntentData() {
        this.filePath = this.activity.getIntent().getStringExtra(FullscreenImageActivity.FILE_PATH);
    }

    private void hideStatusBar() {
        this.activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    private void initView() {
        final File file = new File(this.filePath);
        ImageUtil.renderFileIntoTarget(file, this.activity.getBinding().image);
    }

    @Override
    public void onViewDetached() {
        this.activity = null;
    }

    @Override
    public void onDestroyed() {
        this.activity = null;
    }
}
