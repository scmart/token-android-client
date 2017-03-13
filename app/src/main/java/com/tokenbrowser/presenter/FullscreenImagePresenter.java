package com.tokenbrowser.presenter;

import android.view.WindowManager;

import com.bumptech.glide.Glide;
import com.tokenbrowser.view.activity.FullscreenImageActivity;

import java.io.File;

public class FullscreenImagePresenter implements Presenter<FullscreenImageActivity> {

    private FullscreenImageActivity activity;
    private String filename;

    @Override
    public void onViewAttached(FullscreenImageActivity view) {
        this.activity = view;
        getIntentData();
        hideStatusBar();
        initView();
    }

    private void getIntentData() {
        this.filename = this.activity.getIntent().getStringExtra(FullscreenImageActivity.FILENAME);
    }

    private void hideStatusBar() {
        this.activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    private void initView() {
        final File file = new File(this.activity.getFilesDir(), this.filename);
        Glide.with(this.activity)
                .load(file)
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
