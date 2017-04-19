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

package com.tokenbrowser.util;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.Target;
import com.crashlytics.android.Crashlytics;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.tokenbrowser.R;
import com.tokenbrowser.exception.QrCodeException;
import com.tokenbrowser.manager.network.image.CachedGlideUrl;
import com.tokenbrowser.manager.network.image.ForceLoadGlideUrl;
import com.tokenbrowser.view.BaseApplication;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import rx.Observable;
import rx.Single;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class ImageUtil {

    public static void loadFromNetwork(final String url, final ImageView imageView) {
        if (url == null || imageView == null) return;

        Observable
            .fromCallable(() -> fetchFromNetwork(url, imageView))
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .filter(result -> result != null)
            .toSingle()
            .doOnSubscribe(() -> renderFromCache(url, imageView))
            .subscribe(
                    result -> renderFileIntoTarget(result, imageView),
                    Crashlytics::logException
            );

    }

    private static void renderFromCache(final String url, final ImageView imageView) {
        Glide
            .with(imageView.getContext())
            .load(new CachedGlideUrl(url))
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true);
    }

    @Nullable
    private static File fetchFromNetwork(final String url, final ImageView imageView) {
        try {
            return Glide
                    .with(imageView.getContext())
                    .load(new ForceLoadGlideUrl(url))
                    .downloadOnly(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                    .get();
        } catch (final InterruptedException | ExecutionException e) {
            LogUtil.print(ImageUtil.class, "Unable to fetch from network. " + e.getMessage());
            return null;
        }
    }

    public static void renderFileIntoTarget(final File result, final ImageView imageView) {
        if (hasDetached(imageView)) return;

        Glide
            .with(imageView.getContext())
            .load(result)
            .into(imageView);
    }

    private static boolean hasDetached(final ImageView imageView) {
        final Context context = imageView.getContext();
        if (context instanceof Application) {
            return false;
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return false;
        }

        if (context instanceof FragmentActivity) {
            return ((Activity) context).isDestroyed();
        } else if (context instanceof Activity) {
            return ((Activity) context).isDestroyed();
        } else if (context instanceof ContextWrapper) {
            final Context baseContext = (((ContextWrapper) context).getBaseContext());
            return ((Activity) baseContext).isDestroyed();
        }

        return false;
    }

    public static void forceLoadFromNetwork(final String url, final ImageView imageView) {
        if (url == null || imageView == null) return;
        Glide
            .with(imageView.getContext())
            .load(new ForceLoadGlideUrl(url))
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)
            .into(imageView);
    }

    public static byte[] compressBitmap(final Bitmap bmp){
        final ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }

    public static Single<Bitmap> generateQrCode(final String value) {
        return Single.fromCallable(() -> {
            try {
                return generateQrCodeBitmap(value);
            } catch (final WriterException e) {
                throw new QrCodeException(e);
            }
        });
    }

    private static Bitmap generateQrCodeBitmap(final String value) throws WriterException {
        if (value == null) return null;
        
        final QRCodeWriter writer = new QRCodeWriter();
        final int size = BaseApplication.get().getResources().getDimensionPixelSize(R.dimen.qr_code_size);
        final Map<EncodeHintType, Integer> map = new HashMap<>();
        map.put(EncodeHintType.MARGIN, 0);
        final BitMatrix bitMatrix = writer.encode(value, BarcodeFormat.QR_CODE, size, size, map);
        final int width = bitMatrix.getWidth();
        final int height = bitMatrix.getHeight();
        final Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        final int contrastColour = ContextCompat.getColor(BaseApplication.get(), R.color.windowBackground);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                bmp.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : contrastColour);
            }
        }
        return bmp;
    }
}
