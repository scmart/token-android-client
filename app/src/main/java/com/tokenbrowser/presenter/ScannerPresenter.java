/*
 * 	Copyright (c) 2017. Token Browser, Inc
 *
 * 	This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.tokenbrowser.presenter;

import android.content.Intent;
import android.widget.Toast;

import com.google.zxing.ResultPoint;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.CaptureManager;
import com.tokenbrowser.R;
import com.tokenbrowser.exception.InvalidQrCode;
import com.tokenbrowser.exception.InvalidQrCodePayment;
import com.tokenbrowser.model.local.PermissionResultHolder;
import com.tokenbrowser.model.local.QrCodePayment;
import com.tokenbrowser.model.local.ScanResult;
import com.tokenbrowser.model.local.User;
import com.tokenbrowser.model.sofa.Payment;
import com.tokenbrowser.util.LogUtil;
import com.tokenbrowser.util.PaymentType;
import com.tokenbrowser.util.QrCode;
import com.tokenbrowser.util.QrCodeType;
import com.tokenbrowser.util.SoundManager;
import com.tokenbrowser.view.BaseApplication;
import com.tokenbrowser.view.activity.ChatActivity;
import com.tokenbrowser.view.activity.ScannerActivity;
import com.tokenbrowser.view.activity.ViewUserActivity;
import com.tokenbrowser.view.fragment.DialogFragment.PaymentConfirmationDialog;

import java.util.List;

import rx.Single;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public final class ScannerPresenter implements
        Presenter<ScannerActivity>,
        PaymentConfirmationDialog.OnPaymentConfirmationListener {

    private static final String WEB_SIGNIN = "web-signin:";

    private CaptureManager capture;
    private ScannerActivity activity;
    private CompositeSubscription subscriptions;

    private boolean firstTimeAttaching = true;
    private @PaymentType.Type int paymentType;

    @Override
    public void onViewAttached(final ScannerActivity activity) {
        this.activity = activity;

        if (this.firstTimeAttaching) {
            this.firstTimeAttaching = false;
            initLongLivingObjects();
        }

        init();
    }

    private void initLongLivingObjects() {
        this.subscriptions = new CompositeSubscription();
    }

    private void init() {
        initCloseButton();
        initScanner();
    }

    private void initCloseButton() {
        this.activity.getBinding().closeButton.setOnClickListener(__ -> activity.finish());
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
            final ScanResult scanResult = new ScanResult(result);
            handleScanResult(scanResult.getText());
        }

        @Override
        public void possibleResultPoints(final List<ResultPoint> resultPoints) {}
    };

    private void handleScanResult(final String result) {
        final QrCode qrCode = new QrCode(result);
        final @QrCodeType.Type int qrCodeType = qrCode.getQrCodeType();

        if (qrCodeType == QrCodeType.EXTERNAL) {
            handleExternalQrCode(qrCode);
        } else if (qrCodeType == QrCodeType.ADD) {
            handleAddQrCode(qrCode);
        } else if (qrCodeType == QrCodeType.PAY) {
            handlePaymentQrCode(qrCode);
        } else if (result.startsWith(WEB_SIGNIN)) {
            handleWebLogin(result);
        } else {
            handleInvalidQrCode();
        }
    }

    private void handleExternalQrCode(final QrCode qrCode) {
        if (this.activity == null) return;
        try {
            final QrCodePayment payment = qrCode.getExternalPayment();
            this.paymentType = PaymentType.TYPE_SEND;

            final Subscription sub =
                    BaseApplication
                    .get()
                    .getTokenManager()
                    .getUserManager()
                    .getUserFromPaymentAddress(payment.getAddress())
                    .doOnSuccess(__ -> playScanSound())
                    .doOnError(__ -> playScanSound())
                    .subscribe(
                            user -> showTokenPaymentConfirmationDialog(user.getTokenId(), payment),
                            __ -> showExternalPaymentConfirmationDialog(payment));

            this.subscriptions.add(sub);
        } catch (InvalidQrCodePayment e) {
            handleInvalidQrCode();
        }
    }

    private void showTokenPaymentConfirmationDialog(final String tokenId, final QrCodePayment payment) {
        if (this.activity == null) return;
        try {
            final PaymentConfirmationDialog dialog =
                    PaymentConfirmationDialog
                    .newInstanceTokenPayment(
                            tokenId,
                            payment.getValue(),
                            payment.getMemo()
                    );
            dialog.show(this.activity.getSupportFragmentManager(), PaymentConfirmationDialog.TAG);
            dialog.setOnPaymentConfirmationListener(this);
        } catch (InvalidQrCodePayment e) {
            handleInvalidQrCode();
        }
    }

    private void showExternalPaymentConfirmationDialog(final QrCodePayment payment) {
        if (this.activity == null) return;
        try {
            final PaymentConfirmationDialog dialog =
                    PaymentConfirmationDialog
                    .newInstanceExternalPayment(
                            payment.getAddress(),
                            payment.getValue(),
                            payment.getMemo()
                    );
            dialog.show(this.activity.getSupportFragmentManager(), PaymentConfirmationDialog.TAG);
            dialog.setOnPaymentConfirmationListener(this);
        } catch (InvalidQrCodePayment invalidQrCodePayment) {
            handleInvalidQrCode();
        }
    }

    private void handleAddQrCode(final QrCode qrCode) {
        try {
            final Subscription sub =
                    getUserByUsername(qrCode.getUsername())
                    .doOnSuccess(__ -> playScanSound())
                    .subscribe(
                            user -> goToProfileView(user.getTokenId()),
                            __ -> handleInvalidQrCode()
                    );

            this.subscriptions.add(sub);
        } catch (InvalidQrCode e) {
            handleInvalidQrCode();
        }
    }

    private void goToProfileView(final String tokenId) {
        if (this.activity == null) return;
        final Intent intent = new Intent(this.activity, ViewUserActivity.class)
                .putExtra(ViewUserActivity.EXTRA__USER_ADDRESS, tokenId)
                .putExtra(ViewUserActivity.EXTRA__PLAY_SCAN_SOUNDS, true);
        this.activity.startActivity(intent);
        this.activity.finish();
    }

    private void handlePaymentQrCode(final QrCode qrCode) {
        try {
            final QrCodePayment payment = qrCode.getPayment();
            this.paymentType = PaymentType.TYPE_SEND;

            final Subscription sub =
                    getUserByUsername(payment.getUsername())
                    .doOnSuccess(__ -> playScanSound())
                    .subscribe(
                            user -> showTokenPaymentConfirmationDialog(user.getTokenId(), payment),
                            __ -> handleInvalidQrCode()
                    );

            this.subscriptions.add(sub);
        } catch (InvalidQrCodePayment e) {
            handleInvalidQrCode();
        }
    }

    private Single<User> getUserByUsername(final String username) {
        return BaseApplication
                .get()
                .getTokenManager()
                .getUserManager()
                .getUserByUsername(username)
                .observeOn(AndroidSchedulers.mainThread());
    }

    private void handleInvalidQrCode() {
        if (this.activity == null) return;
        SoundManager.getInstance().playSound(SoundManager.SCAN_ERROR);
        Toast.makeText(
                this.activity,
                this.activity.getString(R.string.invalid_qr_code),
                Toast.LENGTH_SHORT
        ).show();
    }

    private void handleWebLogin(final String result) {
        final String token = result.substring(WEB_SIGNIN.length());
        final Subscription sub =
                loginWithToken(token)
                .doOnSuccess(__ -> playScanSound())
                .subscribe(
                        __ -> handleLoginSuccess(),
                        this::handleLoginFailure
                );

        this.subscriptions.add(sub);
    }

    private Single<Void> loginWithToken(final String token) {
        return BaseApplication
                .get()
                .getTokenManager()
                .getUserManager()
                .webLogin(token)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    private void playScanSound() {
        SoundManager.getInstance().playSound(SoundManager.SCAN);
    }

    private void handleLoginSuccess() {
        Toast.makeText(BaseApplication.get(), R.string.web_signin, Toast.LENGTH_LONG).show();
        if (this.activity == null) return;
        this.activity.finish();
    }

    private void handleLoginFailure(final Throwable throwable) {
        LogUtil.e(getClass(), throwable.toString());
        Toast.makeText(BaseApplication.get(), R.string.error__web_signin, Toast.LENGTH_LONG).show();
    }

    public void handlePermissionsResult(final PermissionResultHolder prh) {
        this.capture.onRequestPermissionsResult(prh.getRequestCode(), prh.getPermissions(), prh.getGrantResults());
    }

    @Override
    public void onPaymentRejected() {
        this.activity.finish();
    }

    @Override
    public void onTokenPaymentApproved(final String tokenId, final Payment payment) {
        try {
            goToChatActivityWithPayment(tokenId, payment);
        } catch (InvalidQrCodePayment e) {
            handleInvalidQrCode();
        }
    }

    private void goToChatActivityWithPayment(final String tokenId, final Payment payment) throws InvalidQrCodePayment {
        final Intent intent = new Intent(activity, ChatActivity.class)
                .putExtra(ChatActivity.EXTRA__REMOTE_USER_ADDRESS, tokenId)
                .putExtra(ChatActivity.EXTRA__PAYMENT_ACTION, this.paymentType)
                .putExtra(ChatActivity.EXTRA__ETH_AMOUNT, payment.getValue())
                .putExtra(ChatActivity.EXTRA__PLAY_SCAN_SOUNDS, true);

        this.activity.startActivity(intent);
        this.activity.finish();
    }

    public void onExternalPaymentApproved(final Payment payment) {
        try {
            sendExternalPayment(payment);
        } catch (InvalidQrCodePayment invalidQrCodePayment) {
            handleInvalidQrCode();
        }
    }

    private void sendExternalPayment(final Payment payment) throws InvalidQrCodePayment {
        BaseApplication
                .get()
                .getTokenManager()
                .getTransactionManager()
                .sendExternalPayment(payment.getToAddress(), payment.getValue());
    }

    @Override
    public void onViewDetached() {
        if (this.capture != null) {
            this.capture.onPause();
        }
        this.subscriptions.clear();
        this.activity = null;
    }

    @Override
    public void onDestroyed() {
        if (this.capture != null) {
            this.capture.onDestroy();
        }
    }
}
