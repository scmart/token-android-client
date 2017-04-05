package com.tokenbrowser.util;

import android.graphics.Bitmap;

import com.tokenbrowser.R;
import com.tokenbrowser.view.BaseApplication;

import rx.Single;

public class QrCodeUtil {

    private static final String PAY_TYPE = "pay";
    private static final String ADD_TYPE = "add";

    public Single<Bitmap> generatePayQrCode(final String username,
                                            final String value,
                                            final String memo) {
        final String baseUrl = BaseApplication.get().getString(R.string.qr_code_base_url);
        final String payParams =
                BaseApplication
                .get()
                .getString(
                    R.string.qr_code_url_pay,
                    PAY_TYPE,
                    username,
                    value,
                    memo
                );

        final String url = String.format("%s%s", baseUrl, payParams);
        return ImageUtil.generateQrCode(url);
    }

    public Single<Bitmap> generateAddQrCode(final String username) {
        final String baseUrl = BaseApplication.get().getString(R.string.qr_code_base_url);
        final String addParams = BaseApplication
                .get()
                .getString(
                    R.string.qr_code_url_add,
                    ADD_TYPE,
                    username
                );

        final String url = String.format("%s%s", baseUrl, addParams);
        return ImageUtil.generateQrCode(url);
    }
}
