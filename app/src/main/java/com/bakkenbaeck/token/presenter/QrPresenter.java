package com.bakkenbaeck.token.presenter;

import android.graphics.Bitmap;
import android.graphics.Color;

import com.bakkenbaeck.token.R;
import com.bakkenbaeck.token.crypto.HDWallet;
import com.bakkenbaeck.token.util.ImageUtil;
import com.bakkenbaeck.token.util.LogUtil;
import com.bakkenbaeck.token.util.SharedPrefsUtil;
import com.bakkenbaeck.token.util.SingleSuccessSubscriber;
import com.bakkenbaeck.token.view.BaseApplication;
import com.bakkenbaeck.token.view.fragment.toplevel.QrFragment;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import rx.Single;
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
        generateAndCacheQrBitmap()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleSuccessSubscriber<Bitmap>() {
                    @Override
                    public void onSuccess(final Bitmap qrBitmap) {
                        renderQrCode(qrBitmap);
                    }
                });
    }

    private Single<Bitmap> generateAndCacheQrBitmap() {
        return Single.fromCallable(new Callable<Bitmap>() {
            @Override
            public Bitmap call() throws Exception {
                try {
                    final Bitmap qrBitmap = generateBitmapFromWalletAddress();
                    SharedPrefsUtil.saveQrCode(ImageUtil.compressBitmap(qrBitmap));
                    return qrBitmap;
                } catch (final WriterException e) {
                    LogUtil.e(getClass(), "Error creating QR code bitmap");
                }
                return null;
            }
        });
    }

    private Bitmap generateBitmapFromWalletAddress() throws WriterException {
        final String prefix = this.fragment.getResources().getString(R.string.prefixEth);
        final QRCodeWriter writer = new QRCodeWriter();
        final int size = this.fragment.getResources().getDimensionPixelSize(R.dimen.qr_code_size);
        final Map<EncodeHintType, Integer> map = new HashMap<>();
        map.put(EncodeHintType.MARGIN, 0);
        final BitMatrix bitMatrix = writer.encode(prefix + this.walletAddress, BarcodeFormat.QR_CODE, size, size, map);
        final int width = bitMatrix.getWidth();
        final int height = bitMatrix.getHeight();
        final Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        final int contrastColour = this.fragment.getResources().getColor(R.color.windowBackground);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                bmp.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : contrastColour);
            }
        }
        return bmp;
    }
}
