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
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationAdapter;
import com.tokenbrowser.R;
import com.tokenbrowser.exception.InvalidQrCode;
import com.tokenbrowser.exception.InvalidQrCodePayment;
import com.tokenbrowser.manager.SofaMessageManager;
import com.tokenbrowser.model.local.QrCodePayment;
import com.tokenbrowser.model.local.User;
import com.tokenbrowser.model.sofa.Payment;
import com.tokenbrowser.util.PaymentType;
import com.tokenbrowser.util.QrCode;
import com.tokenbrowser.util.QrCodeType;
import com.tokenbrowser.util.SharedPrefsUtil;
import com.tokenbrowser.util.SoundManager;
import com.tokenbrowser.view.BaseApplication;
import com.tokenbrowser.view.activity.ChatActivity;
import com.tokenbrowser.view.activity.MainActivity;
import com.tokenbrowser.view.activity.ScannerActivity;
import com.tokenbrowser.view.activity.ViewUserActivity;
import com.tokenbrowser.view.adapter.NavigationAdapter;
import com.tokenbrowser.view.fragment.DialogFragment.PaymentConfirmationDialog;

import rx.Single;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;

public class MainPresenter implements
        Presenter<MainActivity>,
        PaymentConfirmationDialog.OnPaymentConfirmationListener {

    private static final int DEFAULT_TAB = 0;
    private static final int SCAN_POSITION = 2;

    private MainActivity activity;
    private NavigationAdapter adapter;
    private CompositeSubscription subscriptions;
    private boolean firstTimeAttached = true;

    private final AHBottomNavigation.OnTabSelectedListener tabListener = new AHBottomNavigation.OnTabSelectedListener() {
        @Override
        public boolean onTabSelected(final int position, final boolean wasSelected) {
            if (position == SCAN_POSITION) {
                openScanActivity();
                return false;
            }
            final FragmentTransaction transaction = activity.getSupportFragmentManager().beginTransaction();
            transaction.replace(activity.getBinding().fragmentContainer.getId(), adapter.getItem(position)).commit();

            if (!wasSelected) {
                SoundManager.getInstance().playSound(SoundManager.TAB_BUTTON);
            }
            return true;
        }
    };

    @Override
    public void onViewAttached(final MainActivity activity) {
        this.activity = activity;

        if (this.firstTimeAttached) {
            this.firstTimeAttached = false;
            this.adapter = new NavigationAdapter(this.activity, R.menu.navigation);
            this.subscriptions = new CompositeSubscription();
            manuallySelectFirstTab();
        }
        initNavBar();
        trySelectTabFromIntent();
        attachUnreadMessagesSubscription();
        showBetaWarningDialog();
        processIntentData();
    }

    private void manuallySelectFirstTab() {
        this.tabListener.onTabSelected(DEFAULT_TAB, false);
    }

    private void initNavBar() {
        final AHBottomNavigation navBar = this.activity.getBinding().navBar;
        final AHBottomNavigationAdapter menuInflater = new AHBottomNavigationAdapter(this.activity, R.menu.navigation);
        menuInflater.setupWithBottomNavigation(navBar);
        navBar.setTitleState(AHBottomNavigation.TitleState.ALWAYS_SHOW);
        navBar.setAccentColor(ContextCompat.getColor(this.activity, R.color.colorPrimary));
        navBar.setOnTabSelectedListener(this.tabListener);
        navBar.setSoundEffectsEnabled(false);
        navBar.setBehaviorTranslationEnabled(false);
    }

    private void attachUnreadMessagesSubscription() {
        final SofaMessageManager messageManager =
                BaseApplication
                .get()
                .getTokenManager()
                .getSofaMessageManager();

        final Subscription allChangesSubscription =
                messageManager.registerForAllConversationChanges()
                .flatMap((unused) -> messageManager.areUnreadMessages().toObservable())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handleUnreadMessages);

        final Subscription firstTimeSubscription =
                messageManager.areUnreadMessages().toObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handleUnreadMessages);

        this.subscriptions.add(allChangesSubscription);
        this.subscriptions.add(firstTimeSubscription);
    }

    private void handleUnreadMessages(final boolean areUnreadMessages) {
        if (areUnreadMessages) {
            showUnreadBadge();
        } else {
            hideUnreadBadge();
        }
    }

    private void showBetaWarningDialog() {
        if (SharedPrefsUtil.hasLoadedApp()) return;

        final AlertDialog.Builder builder = new AlertDialog.Builder(this.activity, R.style.AlertDialogCustom);
        builder.setTitle(R.string.beta_warning_title)
                .setMessage(R.string.beta_warning_message)
                .setPositiveButton(R.string.continue_, (dialog, which) -> {
                    dialog.dismiss();
                });
        builder.create().show();
        SharedPrefsUtil.setHasLoadedApp();
    }

    private void processIntentData() {
        final Uri data = this.activity.getIntent().getData();
        handleIntentUri(data);
    }

    private void handleIntentUri(final Uri uri) {
        if (uri == null) return;

        final QrCode qrCode = new QrCode(uri.toString());
        final @QrCodeType.Type int qrCodeType = qrCode.getQrCodeType();

        if (qrCodeType == QrCodeType.EXTERNAL) {
            handleExternalPayment(qrCode);
        } else if (qrCodeType == QrCodeType.PAY) {
            handleTokenPayment(qrCode);
        } else if (qrCodeType == QrCodeType.ADD) {
            handleAddTokenUser(qrCode);
        }

        this.activity.getIntent().setData(null);
    }

    private void handleExternalPayment(final QrCode qrCode) {
        try {
            final QrCodePayment payment = qrCode.getExternalPayment();
            final Subscription sub =
                    BaseApplication
                    .get()
                    .getTokenManager()
                    .getUserManager()
                    .getUserFromPaymentAddress(payment.getAddress())
                    .subscribe(
                            user -> showTokenPaymentConfirmationDialog(user.getTokenId(), payment),
                            __ -> showExternalPaymentConfirmationDialog(payment)
                    );

            this.subscriptions.add(sub);
        } catch (InvalidQrCodePayment e) {
            handleInvalidQrCodePayment();
        }
    }

    private void handleTokenPayment(final QrCode qrCode) {
        try {
            final QrCodePayment payment = qrCode.getPayment();
            final Subscription sub =
                    getUserByUsername(payment.getUsername())
                    .subscribe(
                            user -> showTokenPaymentConfirmationDialog(user.getTokenId(), payment),
                            __ -> handleInvalidQrCode()
                    );

            this.subscriptions.add(sub);
        } catch (InvalidQrCodePayment e) {
            handleInvalidQrCode();
        }
    }

    private void showTokenPaymentConfirmationDialog(final String tokenId, final QrCodePayment payment) {
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
            handleInvalidQrCodePayment();
        }
    }

    private void showExternalPaymentConfirmationDialog(final QrCodePayment payment) {
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
        } catch (InvalidQrCodePayment e) {
            handleInvalidQrCodePayment();
        }
    }

    private void handleAddTokenUser(final QrCode qrCode) {
        try {
            final Subscription sub =
                    getUserByUsername(qrCode.getUsername())
                    .subscribe(
                            user -> goToProfileView(user.getTokenId()),
                            __ -> handleInvalidQrCode()
                    );

            this.subscriptions.add(sub);
        } catch (InvalidQrCode e) {
            handleInvalidQrCode();
        }
    }

    private Single<User> getUserByUsername(final String username) {
        return BaseApplication
                .get()
                .getTokenManager()
                .getUserManager()
                .getUserByUsername(username);
    }

    private void goToProfileView(final String tokenId) {
        if (this.activity == null) return;
        final Intent intent = new Intent(this.activity, ViewUserActivity.class)
                .putExtra(ViewUserActivity.EXTRA__USER_ADDRESS, tokenId)
                .putExtra(ViewUserActivity.EXTRA__PLAY_SCAN_SOUNDS, true);
        this.activity.startActivity(intent);
        this.activity.finish();
    }

    private void handleInvalidQrCode() {
        Toast.makeText(this.activity, this.activity.getString(R.string.invalid_qr_code), Toast.LENGTH_SHORT).show();
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
            handleInvalidQrCodePayment();
        }
    }

    private void goToChatActivityWithPayment(final String tokenId, final Payment payment) throws InvalidQrCodePayment {
        final Intent intent = new Intent(activity, ChatActivity.class)
                .putExtra(ChatActivity.EXTRA__REMOTE_USER_ADDRESS, tokenId)
                .putExtra(ChatActivity.EXTRA__PAYMENT_ACTION, PaymentType.TYPE_SEND)
                .putExtra(ChatActivity.EXTRA__ETH_AMOUNT, payment.getValue())
                .putExtra(ChatActivity.EXTRA__PLAY_SCAN_SOUNDS, true);

        this.activity.startActivity(intent);
        this.activity.finish();
    }

    private void handleInvalidQrCodePayment() {
        Toast.makeText(this.activity, this.activity.getString(R.string.invalid_payment), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onExternalPaymentApproved(final Payment payment) {
        try {
            sendExternalPayment(payment);
        } catch (InvalidQrCodePayment invalidQrCodePayment) {
            handleInvalidQrCodePayment();
        }
    }

    private void sendExternalPayment(final Payment payment) throws InvalidQrCodePayment {
        BaseApplication
                .get()
                .getTokenManager()
                .getTransactionManager()
                .sendExternalPayment(payment.getToAddress(), payment.getValue());
    }

    private void showUnreadBadge() {
        this.activity.getBinding().navBar.setNotification(" ", 1);
    }

    private void hideUnreadBadge() {
        this.activity.getBinding().navBar.setNotification("", 1);
    }

    @Override
    public void onViewDetached() {
        this.activity = null;
        this.subscriptions.clear();
    }

    @Override
    public void onDestroyed() {
        this.adapter = null;
    }

    public void onRestoreInstanceState(final Bundle savedInstanceState) {
        trySelectTabFromIntent();
    }

    private void trySelectTabFromIntent() {
        final Intent intent = this.activity.getIntent();
        final int activeTab = intent.getIntExtra(MainActivity.EXTRA__ACTIVE_TAB, this.activity.getBinding().navBar.getCurrentItem());
        this.activity.getIntent().removeExtra(MainActivity.EXTRA__ACTIVE_TAB);
        this.activity.getBinding().navBar.setCurrentItem(activeTab);
    }

    private void openScanActivity() {
        final Intent intent = new Intent(this.activity, ScannerActivity.class);
        this.activity.startActivity(intent);
    }
}
