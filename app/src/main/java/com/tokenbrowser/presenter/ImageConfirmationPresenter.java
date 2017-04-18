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

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

import com.tokenbrowser.util.FileUtil;
import com.tokenbrowser.util.ImageUtil;
import com.tokenbrowser.util.LogUtil;
import com.tokenbrowser.view.activity.ImageConfirmationActivity;

import java.io.File;
import java.io.IOException;

public class ImageConfirmationPresenter implements Presenter<ImageConfirmationActivity> {

    private ImageConfirmationActivity activity;
    private Uri fileUri;
    private File imageFile;

    @Override
    public void onViewAttached(ImageConfirmationActivity view) {
        this.activity = view;
        initShortLivingObjects();
    }

    private void initShortLivingObjects() {
        processIntentData();
        saveFileToLocalStorage();
        initToolbar();
        initClickListeners();
        showImage();
    }

    private void processIntentData() {
        this.fileUri = this.activity.getIntent().getParcelableExtra(ImageConfirmationActivity.FILE_URI);
    }

    private void saveFileToLocalStorage() {
        try {
            this.imageFile = new FileUtil().saveFileFromUri(this.activity, fileUri);
        } catch (IOException e) {
            LogUtil.e(getClass(), "Error during saving file to local storage " + e.getMessage());
        }
    }

    private void initToolbar() {
        final String title = this.imageFile.getName().length() > 15
                ? String.format("%.15s...", this.imageFile.getName())
                : this.imageFile.getName();

        this.activity.getBinding().title.setText(title);
    }

    private void initClickListeners() {
        this.activity.getBinding().closeButton.setOnClickListener(v -> handleBackButtonClicked());
        this.activity.getBinding().approveButton.setOnClickListener(v -> handleConfirmedClicked());
    }

    public void handleBackButtonClicked() {
        deleteFile();
        final Intent intent = new Intent();
        this.activity.setResult(Activity.RESULT_OK, intent);
        this.activity.finish();
    }

    private boolean deleteFile() {
        if (this.imageFile == null) return true;
        return this.imageFile.delete();
    }

    private void handleConfirmedClicked() {
        final Intent intent = new Intent()
                .putExtra(ImageConfirmationActivity.FILE_PATH,
                        this.imageFile.getAbsolutePath());
        this.activity.setResult(Activity.RESULT_OK, intent);
        this.activity.finish();
    }

    private void showImage() {
        ImageUtil.renderFileIntoTarget(this.imageFile, this.activity.getBinding().image);
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
