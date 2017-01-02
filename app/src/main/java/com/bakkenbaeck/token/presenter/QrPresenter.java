package com.bakkenbaeck.token.presenter;

import android.graphics.Bitmap;

import com.bakkenbaeck.token.crypto.HDWallet;
import com.bakkenbaeck.token.util.ImageUtil;
import com.bakkenbaeck.token.util.SharedPrefsUtil;
import com.bakkenbaeck.token.util.SingleSuccessSubscriber;
import com.bakkenbaeck.token.view.BaseApplication;
import com.bakkenbaeck.token.view.fragment.toplevel.QrFragment;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public final class QrPresenter implements Presenter<QrFragment> {

    private QrFragment fragment;
    private String walletAddress;

    @Override
    public void onViewAttached(final QrFragment fragment) {
        this.fragment = fragment;
        init();
    }

    private void init() {
        initWalletDetails();
    }

    private void initWalletDetails() {
        BaseApplication.get()
                .getTokenManager().getWallet()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleSuccessSubscriber<HDWallet>() {
                    @Override
                    public void onSuccess(final HDWallet wallet) {
                        QrPresenter.this.walletAddress = wallet.getAddress();
                        initView();
                        this.unsubscribe();
                    }
                });
    }

    @Override
    public void onViewDetached() {
        this.fragment = null;
    }

    @Override
    public void onViewDestroyed() {
        this.fragment = null;
    }

    private void initView() {
        this.fragment.getBinding().qrCodeText.setText(this.walletAddress);

        final byte[] decodedBitmap = SharedPrefsUtil.getQrCode();
        if (decodedBitmap != null) {
            renderQrCode(decodedBitmap);
        } else {
            generateQrCode();
        }
    }

    private void renderQrCode(final byte[] qrCodeImageBytes) {
        final Bitmap qrCodeBitmap = ImageUtil.decodeByteArray(qrCodeImageBytes);
        renderQrCode(qrCodeBitmap);
    }

    private void renderQrCode(final Bitmap qrCodeBitmap) {
        this.fragment.getBinding().qrCodeImage.setAlpha(0.0f);
        this.fragment.getBinding().qrCodeImage.setImageBitmap(qrCodeBitmap);
        this.fragment.getBinding().qrCodeImage.animate().alpha(1f).setDuration(200).start();
    }

    private void generateQrCode() {
        ImageUtil.generateQrCodeForWalletAddress(this.walletAddress)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleSuccessSubscriber<Bitmap>() {
                    @Override
                    public void onSuccess(final Bitmap qrBitmap) {
                        SharedPrefsUtil.saveQrCode(ImageUtil.compressBitmap(qrBitmap));
                        renderQrCode(qrBitmap);
                    }
                });
    }
}
