package com.bakkenbaeck.token.presenter;

import android.content.Intent;

import com.bakkenbaeck.token.model.ScanResult;
import com.bakkenbaeck.token.view.activity.ViewUserActivity;
import com.bakkenbaeck.token.view.fragment.toplevel.ScannerFragment;
import com.google.zxing.ResultPoint;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.CaptureManager;

import java.util.List;

public final class ScannerPresenter implements Presenter<ScannerFragment> {

    private CaptureManager capture;
    private ScannerFragment fragment;

    @Override
    public void onViewAttached(final ScannerFragment fragment) {
        this.fragment = fragment;
        init();
    }

    private void init() {
        this.capture = new CaptureManager(this.fragment.getActivity(), this.fragment.getBinding().scanner);
        this.fragment.getBinding().scanner.decodeSingle(this.onScanSuccess);
        this.capture.onResume();
    }

    private final BarcodeCallback onScanSuccess = new BarcodeCallback() {
        @Override
        public void barcodeResult(final BarcodeResult result) {
            // Right now, this assumes that the QR code is a contacts address
            // so it is currently very naive
            final ScanResult scanResult = new ScanResult(result);
            final Intent intent = new Intent(fragment.getActivity(), ViewUserActivity.class);
            intent.putExtra(ViewUserActivity.EXTRA__USER_ADDRESS, scanResult.getText());
            fragment.startActivity(intent);
        }

        @Override
        public void possibleResultPoints(final List<ResultPoint> resultPoints) {

        }
    };

    @Override
    public void onViewDetached() {
        this.capture.onPause();
        this.fragment = null;
    }

    @Override
    public void onViewDestroyed() {
        this.capture.onDestroy();
        this.fragment = null;
    }
}
