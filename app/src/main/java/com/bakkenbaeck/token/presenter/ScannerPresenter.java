package com.bakkenbaeck.token.presenter;

import android.content.Intent;
import android.view.View;

import com.bakkenbaeck.token.model.local.ScanResult;
import com.bakkenbaeck.token.view.activity.ScannerActivity;
import com.bakkenbaeck.token.view.activity.ViewUserActivity;
import com.google.zxing.ResultPoint;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.CaptureManager;

import java.util.List;

public final class ScannerPresenter implements Presenter<ScannerActivity> {

    private CaptureManager capture;
    private ScannerActivity activity;

    @Override
    public void onViewAttached(final ScannerActivity activity) {
        this.activity = activity;
        init();
    }

    private void init() {
        initCloseButton();
        initScanner();
    }

    private void initCloseButton() {
        this.activity.getBinding().closeButton.setOnClickListener((View v) -> activity.finish());
    }

    private void initScanner() {
        if (this.capture == null) {
            this.capture = new CaptureManager(this.activity, this.activity.getBinding().scanner);
        }
        this.activity.getBinding().scanner.decodeSingle(this.onScanSuccess);
        this.capture.onResume();
    }

    private final BarcodeCallback onScanSuccess = new BarcodeCallback() {
        @Override
        public void barcodeResult(final BarcodeResult result) {
            // Right now, this assumes that the QR code is a contacts address
            // so it is currently very naive
            final ScanResult scanResult = new ScanResult(result);
            final Intent intent = new Intent(activity, ViewUserActivity.class);
            intent.putExtra(ViewUserActivity.EXTRA__USER_ADDRESS, scanResult.getText());
            activity.startActivity(intent);
        }

        @Override
        public void possibleResultPoints(final List<ResultPoint> resultPoints) {

        }
    };

    @Override
    public void onViewDetached() {
        if (this.capture != null) {
            this.capture.onPause();
        }
        this.activity = null;
    }

    @Override
    public void onViewDestroyed() {
        if (this.capture != null) {
            this.capture.onDestroy();
        }
        this.activity = null;
    }
}
