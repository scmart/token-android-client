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

import android.graphics.Bitmap;
import android.net.Uri;

import com.tokenbrowser.R;
import com.tokenbrowser.exception.InvalidQrCode;
import com.tokenbrowser.exception.InvalidQrCodePayment;
import com.tokenbrowser.model.local.QrCodePayment;
import com.tokenbrowser.view.BaseApplication;

import rx.Single;

public class QrCode {

    private static final String PAY_TYPE = "pay";
    private static final String ADD_TYPE = "add";
    private static final String EXTERNAL_URL_PREFIX = "ethereum:";
    private static final String VALUE = "value";
    private static final String MEMO = "memo";

    private String url;

    public QrCode(final String url) {
        this.url = url;
    }

    public @QrCodeType.Type int getQrCodeType() {
        final String baseUrl = BaseApplication.get().getString(R.string.qr_code_base_url);
        if (this.url.startsWith(baseUrl + PAY_TYPE)) {
            return QrCodeType.PAY;
        } else if (this.url.startsWith(baseUrl + ADD_TYPE)) {
            return QrCodeType.ADD;
        } else if (this.url.startsWith(EXTERNAL_URL_PREFIX)) {
            return QrCodeType.EXTERNAL;
        } else {
            return QrCodeType.INVALID;
        }
    }

    public String getUsername() throws InvalidQrCode {
        try {
            final String username = Uri.parse(this.url).getLastPathSegment();
            final String usernameWithoutPrefix = username.startsWith("@")
                    ? username.replaceFirst("@", "")
                    : null;
            if (usernameWithoutPrefix != null) return usernameWithoutPrefix;
            else throw new InvalidQrCode();
        } catch (UnsupportedOperationException e) {
            throw new InvalidQrCode(e);
        }
    }

    public QrCodePayment getPayment() throws InvalidQrCodePayment {
        try {
            final String username = getUsername();
            final QrCodePayment payment = getPaymentWithParams()
                    .setUsername(username);
            if (payment.isValid()) return payment;
            else throw new InvalidQrCodePayment();
        } catch (UnsupportedOperationException | InvalidQrCode e) {
            throw new InvalidQrCodePayment(e);
        }
    }

    public QrCodePayment getExternalPayment() throws InvalidQrCodePayment {
        try {
            final String baseUrl = String.format("%s%s/", BaseApplication.get().getString(R.string.qr_code_base_url), PAY_TYPE);
            this.url = this.url.replaceFirst(EXTERNAL_URL_PREFIX, baseUrl);
            final String address = Uri.parse(this.url).getLastPathSegment();
            return getPaymentWithParams()
                    .setAddress(address);
        } catch (UnsupportedOperationException e) {
            throw new InvalidQrCodePayment(e);
        }
    }

    private QrCodePayment getPaymentWithParams() throws UnsupportedOperationException {
        final Uri uri = Uri.parse(this.url);
        final String value = uri.getQueryParameter(VALUE);
        final String memo = uri.getQueryParameter(MEMO);
        return new QrCodePayment()
                .setValue(value)
                .setMemo(memo);
    }

    public static Single<Bitmap> generateAddQrCode(final String username) {
        final String baseUrl = BaseApplication.get().getString(R.string.qr_code_base_url);
        final String addParams = getAddUrl(username);
        final String url = String.format("%s%s", baseUrl, addParams);
        return ImageUtil.generateQrCode(url);
    }

    private static String getAddUrl(final String username) {
        return BaseApplication
                .get()
                .getString(
                        R.string.qr_code_add_url,
                        ADD_TYPE,
                        username
                );
    }

    public static Single<Bitmap> generatePayQrCode(final String username,
                                                   final String value,
                                                   final String memo) {
        final String baseUrl = BaseApplication.get().getString(R.string.qr_code_base_url);
        final String payParams = memo != null
                ? getPayUrl(username, value, memo)
                : getPayUrl(username, value);
        final String url = String.format("%s%s", baseUrl, payParams);
        return ImageUtil.generateQrCode(url);
    }

    private static String getPayUrl(final String username,
                                    final String value,
                                    final String memo) {
        return BaseApplication
                .get()
                .getString(
                        R.string.qr_code_pay_url,
                        PAY_TYPE,
                        username,
                        value,
                        memo
                );
    }

    private static String getPayUrl(final String username,
                                    final String value) {
        return BaseApplication
                .get()
                .getString(
                        R.string.qr_code_pay_url_without_memo,
                        PAY_TYPE,
                        username,
                        value
                );
    }
}
