package com.bakkenbaeck.token.presenter;

import android.content.Intent;

import com.bakkenbaeck.token.view.activity.ScanResultActivity;
import com.bakkenbaeck.token.view.fragment.toplevel.ScannerFragment;
import com.google.zxing.ResultPoint;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.CaptureManager;

import java.util.List;

public final class ScanResultPresenter implements Presenter<ScanResultActivity> {

    private ScanResultActivity activity;

    @Override
    public void onViewAttached(final ScanResultActivity activity) {
        this.activity = activity;
        init();
    }

    private void init() {
    }


    @Override
    public void onViewDetached() {
        this.activity = null;
    }

    @Override
    public void onViewDestroyed() {
        this.activity = null;
    }
}
