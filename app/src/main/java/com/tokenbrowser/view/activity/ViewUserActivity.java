package com.tokenbrowser.view.activity;


import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.tokenbrowser.R;
import com.tokenbrowser.databinding.ActivityScanResultBinding;
import com.tokenbrowser.model.local.ActivityResultHolder;
import com.tokenbrowser.presenter.ViewUserPresenter;
import com.tokenbrowser.presenter.factory.PresenterFactory;
import com.tokenbrowser.presenter.factory.ViewUserPresenterFactory;

public class ViewUserActivity extends BasePresenterActivity<ViewUserPresenter, ViewUserActivity> {
    public static final String EXTRA__USER_ADDRESS = "extra_user_address";
    public static final String EXTRA__PLAY_SCAN_SOUNDS = "play_scan_sounds";

    private ActivityScanResultBinding binding;
    private ViewUserPresenter presenter;
    private ActivityResultHolder resultHolder;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    private void init() {
        this.binding = DataBindingUtil.setContentView(this, R.layout.activity_scan_result);
    }

    @Override
    public void onResume() {
        super.onResume();
        tryProcessResultHolder();
    }

    public ActivityScanResultBinding getBinding() {
        return this.binding;
    }

    @NonNull
    @Override
    protected PresenterFactory<ViewUserPresenter> getPresenterFactory() {
        return new ViewUserPresenterFactory();
    }

    @Override
    protected void onPresenterPrepared(@NonNull final ViewUserPresenter presenter) {
        this.presenter = presenter;
        tryProcessResultHolder();
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
        return 3001;
    }
}
