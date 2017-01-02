package com.bakkenbaeck.token.presenter;

import android.content.Intent;
import android.graphics.Bitmap;

import com.bakkenbaeck.token.model.ScanResult;
import com.bakkenbaeck.token.util.ImageUtil;
import com.bakkenbaeck.token.util.SingleSuccessSubscriber;
import com.bakkenbaeck.token.view.activity.ScanResultActivity;
import com.bakkenbaeck.token.view.fragment.toplevel.ScannerFragment;
import com.google.zxing.ResultPoint;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.CaptureManager;

import java.util.List;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public final class ScanResultPresenter implements Presenter<ScanResultActivity> {

    private boolean firstTimeAttached = true;
    private ScanResultActivity activity;

    @Override
    public void onViewAttached(final ScanResultActivity activity) {
        this.activity = activity;
        if (this.firstTimeAttached) {
            this.firstTimeAttached = false;
            init();
        }
    }

    private void init() {
        final Intent intent = activity.getIntent();
        final ScanResult scanResult = intent.getParcelableExtra(ScanResultActivity.EXTRA__RESULT);
        scanResult.getQrCode()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this.handleQrCodeLoaded);
    }

    private final SingleSuccessSubscriber<Bitmap> handleQrCodeLoaded = new SingleSuccessSubscriber<Bitmap>() {
        @Override
        public void onSuccess(final Bitmap qrBitmap) {
            renderQrCode(qrBitmap);
        }
    };

    private void renderQrCode(final Bitmap qrCodeBitmap) {
        this.activity.getBinding().qrCodeImage.setAlpha(0.0f);
        this.activity.getBinding().qrCodeImage.setImageBitmap(qrCodeBitmap);
        this.activity.getBinding().qrCodeImage.animate().alpha(1f).setDuration(200).start();
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
