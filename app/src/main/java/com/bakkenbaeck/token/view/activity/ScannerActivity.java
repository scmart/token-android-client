package com.bakkenbaeck.token.view.activity;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.bakkenbaeck.token.R;
import com.bakkenbaeck.token.databinding.ActivityScannerBinding;
import com.bakkenbaeck.token.model.local.PermissionResultHolder;
import com.bakkenbaeck.token.presenter.ScannerPresenter;
import com.bakkenbaeck.token.presenter.factory.PresenterFactory;
import com.bakkenbaeck.token.presenter.factory.ScannerPresenterFactory;

public class ScannerActivity extends BasePresenterActivity<ScannerPresenter, ScannerActivity> {

    public static final int FOR_RESULT = 1;
    public static final int REDIRECT = 2;
    public static final String RESULT_TYPE = "result_type";

    private ScannerPresenter presenter;
    private ActivityScannerBinding binding;
    private PermissionResultHolder resultHolder;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    private void init() {
        this.binding = DataBindingUtil.setContentView(this, R.layout.activity_scanner);
    }

    public ActivityScannerBinding getBinding() {
        return this.binding;
    }

    @NonNull
    @Override
    protected PresenterFactory<ScannerPresenter> getPresenterFactory() {
        return new ScannerPresenterFactory();
    }

    @Override
    protected void onPresenterPrepared(@NonNull final ScannerPresenter presenter) {
        this.presenter = presenter;
        tryHandlePermissionResultHolder();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        this.resultHolder = new PermissionResultHolder(requestCode, permissions, grantResults);
        tryHandlePermissionResultHolder();
    }

    private void tryHandlePermissionResultHolder() {
        if (this.resultHolder == null) {
            return;
        }

        if (this.presenter == null) {
            return;
        }

        this.presenter.handlePermissionsResult(this.resultHolder);
        this.resultHolder = null;
    }

    @Override
    protected int loaderId() {
        return hashCode();
    }
}