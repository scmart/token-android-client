package com.tokenbrowser.view.activity;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;

import com.tokenbrowser.token.R;
import com.tokenbrowser.token.databinding.ActivityScannerBinding;
import com.tokenbrowser.model.local.PermissionResultHolder;
import com.tokenbrowser.presenter.ScannerPresenter;
import com.tokenbrowser.presenter.factory.PresenterFactory;
import com.tokenbrowser.presenter.factory.ScannerPresenterFactory;
import com.tokenbrowser.view.custom.OfflineViewRenderer;

public class ScannerActivity
        extends OfflineViewBasePresenterActivity<ScannerPresenter, ScannerActivity>
        implements OfflineViewRenderer {

    public static final int FOR_RESULT = 1;
    public static final int REDIRECT = 2;
    public static final int CONFIRMATION_REDIRECT = 3;
    public static final String RESULT_TYPE = "result_type";
    public static final String ETH_AMOUNT = "eth_amount";
    public static final String PAYMENT_TYPE = "payment_type";

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

    @Override
    public View getOfflineViewContainer() {
        return this.binding.getRoot();
    }
}