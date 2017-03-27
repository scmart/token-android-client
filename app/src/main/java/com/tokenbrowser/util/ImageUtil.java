package com.tokenbrowser.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;

import com.tokenbrowser.token.R;
import com.tokenbrowser.view.BaseApplication;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import rx.Single;

public class ImageUtil {

    public static Bitmap decodeByteArray(final byte[] bytes){
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    public static byte[] compressBitmap(final Bitmap bmp){
        final ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }

    public static Single<Bitmap> generateQrCodeForWalletAddress(final String walletAddress) {
        return Single.fromCallable(new Callable<Bitmap>() {
            @Override
            public Bitmap call() throws Exception {
                try {
                    return generateQrCodeBitmap(walletAddress);
                } catch (final WriterException e) {
                    LogUtil.e(getClass(), "Error creating QR code bitmap");
                }
                return null;
            }
        });
    }

    private static Bitmap generateQrCodeBitmap(final String walletAddress) throws WriterException {
        if (walletAddress == null) {
            return null;
        }
        
        final QRCodeWriter writer = new QRCodeWriter();
        final int size = BaseApplication.get().getResources().getDimensionPixelSize(R.dimen.qr_code_size);
        final Map<EncodeHintType, Integer> map = new HashMap<>();
        map.put(EncodeHintType.MARGIN, 0);
        final BitMatrix bitMatrix = writer.encode(walletAddress, BarcodeFormat.QR_CODE, size, size, map);
        final int width = bitMatrix.getWidth();
        final int height = bitMatrix.getHeight();
        final Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        final int contrastColour = BaseApplication.get().getResources().getColor(R.color.windowBackground);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                bmp.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : contrastColour);
            }
        }
        return bmp;
    }
}
