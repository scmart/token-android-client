package com.bakkenbaeck.token.view.activity;


import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.bakkenbaeck.token.R;
import com.bakkenbaeck.token.databinding.ActivityScanResultBinding;
import com.bakkenbaeck.token.presenter.ScanResultPresenter;
import com.bakkenbaeck.token.presenter.factory.PresenterFactory;
import com.bakkenbaeck.token.presenter.factory.ScanResultPresenterFactory;

public class ScanResultActivity extends BasePresenterActivity<ScanResultPresenter, ScanResultActivity> {
    public static final String EXTRA__RESULT = "extra_result";

    private ActivityScanResultBinding binding;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    private void init() {
        this.binding = DataBindingUtil.setContentView(this, R.layout.activity_scan_result);
    }

    public ActivityScanResultBinding getBinding() {
        return this.binding;
    }


    @NonNull
    @Override
    protected PresenterFactory<ScanResultPresenter> getPresenterFactory() {
        return new ScanResultPresenterFactory();
    }

    @Override
    protected void onPresenterPrepared(@NonNull final ScanResultPresenter presenter) {

    }

    @Override
    protected int loaderId() {
        return 3001;
    }
}
