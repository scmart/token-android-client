package com.bakkenbaeck.token.view.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bakkenbaeck.token.R;
import com.bakkenbaeck.token.presenter.ScannerPresenter;
import com.bakkenbaeck.token.presenter.factory.PresenterFactory;
import com.bakkenbaeck.token.presenter.factory.ScannerPresenterFactory;
import com.journeyapps.barcodescanner.CaptureManager;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;

public class ScannerFragment extends BasePresenterFragment<ScannerPresenter, ScannerFragment>  {
    private CaptureManager capture;
    private DecoratedBarcodeView barcodeScannerView;
    private ScannerPresenter presenter;

    public static Fragment newInstance() {
        return new ScannerFragment();
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater,
                             final ViewGroup container,
                             final Bundle inState) {
        final View v = inflater.inflate(R.layout.activity_barcode, container, false);
        barcodeScannerView = (DecoratedBarcodeView)v.findViewById(R.id.barcode_scanner);

        capture = new CaptureManager(this.getActivity(), barcodeScannerView);
        capture.decode();
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        capture.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        capture.onPause();
    }

    @NonNull
    @Override
    protected PresenterFactory<ScannerPresenter> getPresenterFactory() {
        return new ScannerPresenterFactory();
    }

    @Override
    protected void onPresenterPrepared(@NonNull final ScannerPresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        capture.onDestroy();
    }

}