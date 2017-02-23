package com.bakkenbaeck.token.presenter;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.Toast;

import com.bakkenbaeck.token.R;
import com.bakkenbaeck.token.model.local.PermissionResultHolder;
import com.bakkenbaeck.token.model.local.ScanResult;
import com.bakkenbaeck.token.model.network.ServerTime;
import com.bakkenbaeck.token.network.IdService;
import com.bakkenbaeck.token.util.PaymentType;
import com.bakkenbaeck.token.util.SoundManager;
import com.bakkenbaeck.token.view.BaseApplication;
import com.bakkenbaeck.token.view.activity.ChatActivity;
import com.bakkenbaeck.token.view.activity.ScannerActivity;
import com.bakkenbaeck.token.view.activity.ViewUserActivity;
import com.bakkenbaeck.token.view.fragment.DialogFragment.PaymentRequestConfirmationDialog;
import com.google.zxing.ResultPoint;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.CaptureManager;

import java.util.List;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public final class ScannerPresenter implements
        Presenter<ScannerActivity>,
        PaymentRequestConfirmationDialog.OnActionClickListener {

    /*package */ static final String USER_ADDRESS = "user_address";
    private static final String WEB_SIGNIN = "web-signin:";

    private CaptureManager capture;
    private ScannerActivity activity;
    private int resultType;
    private String encodedEthAmount;
    private @PaymentType.Type int paymentType;

    @Override
    public void onViewAttached(final ScannerActivity activity) {
        this.activity = activity;
        getIntentData();
        init();
    }

    @SuppressWarnings("WrongConstant")
    private void getIntentData() {
        this.resultType = this.activity.getIntent().getIntExtra(ScannerActivity.RESULT_TYPE, 0);
        this.encodedEthAmount = this.activity.getIntent().getStringExtra(ScannerActivity.ETH_AMOUNT);
        this.paymentType = this.activity.getIntent().getIntExtra(ScannerActivity.PAYMENT_TYPE, PaymentType.TYPE_SEND);
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
            SoundManager.getInstance().playSound(SoundManager.SCAN_RESULT);
            // Right now, this assumes that the QR code is a contacts address
            // so it is currently very naive
            final ScanResult scanResult = new ScanResult(result);
            if (resultType == ScannerActivity.CONFIRMATION_REDIRECT) {
                showConfirmationDialog(scanResult);
            } else if (resultType == ScannerActivity.FOR_RESULT) {
                final Intent intent = new Intent();
                intent.putExtra(USER_ADDRESS, scanResult.getText());
                activity.setResult(Activity.RESULT_OK, intent);
                activity.finish();
            } else {
                if (scanResult.getText().startsWith(WEB_SIGNIN)) {
                    final String token = scanResult.getText().substring(WEB_SIGNIN.length());
                    webLoginWithToken(token);
                } else {
                    final Intent intent = new Intent(activity, ViewUserActivity.class);
                    intent.putExtra(ViewUserActivity.EXTRA__USER_ADDRESS, scanResult.getText());
                    activity.startActivity(intent);
                }
            }
        }

        @Override
        public void possibleResultPoints(final List<ResultPoint> resultPoints) {
            // 3 == the three eyes of a QR code; it means we only play this sound when
            // we're close to looking at a QR code but haven't read it yet.
            final int minimumPointsRequiredToPlaySound = 3;
            if (resultPoints.size() >= minimumPointsRequiredToPlaySound) {
                SoundManager.getInstance().playSound(SoundManager.SCAN);
            }
        }
    };

    private void showConfirmationDialog(final ScanResult scanResult) {
        final PaymentRequestConfirmationDialog dialog = PaymentRequestConfirmationDialog
                .newInstance(scanResult.getText(), this.encodedEthAmount, this.paymentType);
        dialog.setOnActionClickedListener(this);
        dialog.show(activity.getSupportFragmentManager(), PaymentRequestConfirmationDialog.TAG);
    }

    @Override
    public void onApproved(final String userAddress) {
        final Intent intent = new Intent(activity, ChatActivity.class)
                .putExtra(ChatActivity.EXTRA__REMOTE_USER_ADDRESS, userAddress)
                .putExtra(ChatActivity.EXTRA__PAYMENT_ACTION, this.paymentType)
                .putExtra(ChatActivity.EXTRA__ETH_AMOUNT, this.encodedEthAmount);

        this.activity.startActivity(intent);
        this.activity.finish();
    }

    @Override
    public void onRejected() {
        this.activity.finish();
    }

    public void handlePermissionsResult(final PermissionResultHolder prh) {
        this.capture.onRequestPermissionsResult(prh.getRequestCode(), prh.getPermissions(), prh.getGrantResults());
    }

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

    private void webLoginWithToken(final String loginToken) {
        IdService
            .getApi()
            .getTimestamp()
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .subscribe((st) -> handleFetchTime(st, loginToken), this::handleLoginFailure);
    }

    private void handleFetchTime(final ServerTime serverTime, final String loginToken) {
        if (serverTime == null) {
            handleLoginFailure(new Throwable("ServerTime was null"));
        }

        IdService
            .getApi()
            .webLogin(loginToken, serverTime.get())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(this::handleLoginSuccess, this::handleLoginFailure);
    }

    private void handleLoginFailure(final Throwable throwable) {
        Toast.makeText(BaseApplication.get(), R.string.error__web_signin, Toast.LENGTH_LONG).show();
    }

    private void handleLoginSuccess(final Void unused) {
        Toast.makeText(BaseApplication.get(), R.string.web_signin, Toast.LENGTH_LONG).show();
        if (activity != null) {
            activity.finish();
        }
    }
}
