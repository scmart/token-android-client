package com.bakkenbaeck.token.presenter;

import com.bakkenbaeck.token.view.fragment.toplevel.ScannerFragment;
import com.journeyapps.barcodescanner.CaptureManager;

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
        this.capture.decode();
        this.capture.onResume();
    }

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
