package com.bakkenbaeck.token.presenter;

import android.content.Intent;
import android.widget.Toast;

import com.bakkenbaeck.token.model.ScanResult;
import com.bakkenbaeck.token.util.LogUtil;
import com.bakkenbaeck.token.view.activity.ChatActivity;
import com.bakkenbaeck.token.view.activity.ScanResultActivity;
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
            final ScanResult scanResult = new ScanResult(result);
            final Intent intent = new Intent(fragment.getActivity(), ScanResultActivity.class);
            intent.putExtra(ScanResultActivity.EXTRA__RESULT, scanResult);
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
