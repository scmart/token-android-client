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
