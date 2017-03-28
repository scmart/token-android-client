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
