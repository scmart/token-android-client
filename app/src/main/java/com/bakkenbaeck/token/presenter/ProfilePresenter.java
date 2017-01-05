package com.bakkenbaeck.token.presenter;

import android.graphics.Bitmap;
import android.view.View;

import com.bakkenbaeck.token.crypto.HDWallet;
import com.bakkenbaeck.token.util.ImageUtil;
import com.bakkenbaeck.token.util.OnSingleClickListener;
import com.bakkenbaeck.token.util.SharedPrefsUtil;
import com.bakkenbaeck.token.util.SingleSuccessSubscriber;
import com.bakkenbaeck.token.view.BaseApplication;
import com.bakkenbaeck.token.view.activity.ProfileActivity;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public final class ProfilePresenter implements Presenter<ProfileActivity> {

    private ProfileActivity activity;
    private String walletAddress;

    @Override
    public void onViewAttached(final ProfileActivity fragment) {
        this.activity = fragment;

        init();
    }

    private void init() {
        initToolbar();
        initView();
    }

    private void initToolbar() {
        this.activity.getBinding().closeButton.setOnClickListener(this.onCloseClicked);
    }

    private void initView() {
        if (this.walletAddress == null) {
            fetchWalletDetails();
            return;
        }

        this.activity.getBinding().username.setText(this.walletAddress);

        final byte[] decodedBitmap = SharedPrefsUtil.getQrCode();
        if (decodedBitmap != null) {
            renderQrCode(decodedBitmap);
        } else {
            generateQrCode();
        }
    }

    private void fetchWalletDetails() {
        BaseApplication.get()
                .getTokenManager().getWallet()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this.handleWalletLoaded);
    }

    private SingleSuccessSubscriber<HDWallet> handleWalletLoaded = new SingleSuccessSubscriber<HDWallet>() {
        @Override
        public void onSuccess(final HDWallet wallet) {
            ProfilePresenter.this.walletAddress = wallet.getAddress();
            initView();
            this.unsubscribe();
        }
    };

    private void renderQrCode(final byte[] qrCodeImageBytes) {
        final Bitmap qrCodeBitmap = ImageUtil.decodeByteArray(qrCodeImageBytes);
        renderQrCode(qrCodeBitmap);
    }

    private void renderQrCode(final Bitmap qrCodeBitmap) {
        this.activity.getBinding().qrCodeImage.setAlpha(0.0f);
        this.activity.getBinding().qrCodeImage.setImageBitmap(qrCodeBitmap);
        this.activity.getBinding().qrCodeImage.animate().alpha(1f).setDuration(200).start();
    }

    private void generateQrCode() {
        ImageUtil.generateQrCodeForWalletAddress(this.walletAddress)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this.handleQrCodeGenerated);
    }

    private SingleSuccessSubscriber<Bitmap> handleQrCodeGenerated = new SingleSuccessSubscriber<Bitmap>() {
        @Override
        public void onSuccess(final Bitmap qrBitmap) {
            SharedPrefsUtil.saveQrCode(ImageUtil.compressBitmap(qrBitmap));
            renderQrCode(qrBitmap);
        }
    };

    private final OnSingleClickListener onCloseClicked = new OnSingleClickListener() {
        @Override
        public void onSingleClick(final View v) {
            activity.onBackPressed();
        }
    };

    @Override
    public void onViewDetached() {
        this.activity = null;
    }

    @Override
    public void onViewDestroyed() {
        this.activity = null;
        this.handleWalletLoaded.unsubscribe();
        this.handleQrCodeGenerated.unsubscribe();
    }
}
