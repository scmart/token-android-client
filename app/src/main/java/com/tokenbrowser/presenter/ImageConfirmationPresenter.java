package com.tokenbrowser.presenter;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

import com.bumptech.glide.Glide;
import com.tokenbrowser.util.FileUtil;
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

    private void deleteFile() {
        if (this.imageFile == null) return;
        this.imageFile.delete();
    }

    private void handleConfirmedClicked() {
        final Intent intent = new Intent()
                .putExtra(ImageConfirmationActivity.FILE_PATH,
                        this.imageFile.getAbsolutePath());
        this.activity.setResult(Activity.RESULT_OK, intent);
        this.activity.finish();
    }

    private void showImage() {
        Glide.with(this.activity)
                .load(this.imageFile)
                .into(this.activity.getBinding().image);
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
