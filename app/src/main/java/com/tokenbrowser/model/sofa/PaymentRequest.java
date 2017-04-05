package com.tokenbrowser.model.sofa;


import android.support.annotation.IntDef;

import com.squareup.moshi.Json;
import com.tokenbrowser.crypto.util.TypeConverter;
import com.tokenbrowser.R;
import com.tokenbrowser.util.EthUtil;
import com.tokenbrowser.util.LocaleUtil;
import com.tokenbrowser.view.BaseApplication;

import java.math.BigDecimal;
import java.math.BigInteger;

import rx.Single;

/**
 * PaymentRequest
 * <p>
 * Request an Ethereum transaction
 *
 */
public class PaymentRequest {

    @IntDef({
            PENDING,
            ACCEPTED,
            REJECTED,
    })
    public @interface State {}
    public static final int PENDING = 0;
    public static final int REJECTED = 1;
    public static final int ACCEPTED = 2;

    private String value;
    private String destinationAddress;
    private String body;

    @Json(name = SofaType.LOCAL_ONLY_PAYLOAD)
    private ClientSideCustomData androidClientSideCustomData;

    public PaymentRequest setValue(final String value) {
        this.value = value;
        return this;
    }

    public Single<PaymentRequest> generateLocalPrice() {
        final BigInteger weiAmount = TypeConverter.StringHexToBigInteger(this.value);
        final BigDecimal ethAmount = EthUtil.weiToEth(weiAmount);
        return BaseApplication
                .get()
                .getTokenManager()
                .getBalanceManager()
                .convertEthToLocalCurrencyString(ethAmount)
                .map(this::setLocalPrice);
    }

    public PaymentRequest setDestinationAddress(final String destinationAddress) {
        this.destinationAddress = destinationAddress;
        return this;
    }

    public String getDestinationAddresss() {
        return this.destinationAddress;
    }

    public String getValue() {
        return this.value;
    }

    public String getBody() {
        return this.body;
    }

    private PaymentRequest setLocalPrice(final String localPrice) {
        if (this.androidClientSideCustomData == null) {
            this.androidClientSideCustomData = new ClientSideCustomData();
        }

        this.androidClientSideCustomData.localPrice = localPrice;
        return this;
    }

    public String getLocalPrice() {
        if (this.androidClientSideCustomData == null) {
            return null;
        }

        return this.androidClientSideCustomData.localPrice;
    }

    public PaymentRequest setState(final @State int state) {
        if (this.androidClientSideCustomData == null) {
            this.androidClientSideCustomData = new ClientSideCustomData();
        }

        this.androidClientSideCustomData.state = state;
        return this;
    }

    public @State int getState() {
        if (this.androidClientSideCustomData == null) {
            return PENDING;
        }

        return this.androidClientSideCustomData.state;
    }

    public String toUserVisibleString(final boolean sentByLocal) {
        final int stringId = sentByLocal
                ? R.string.latest_message__request_outgoing
                : R.string.latest_message__request_incoming;
        return String.format(
                LocaleUtil.getLocale(),
                BaseApplication.get().getResources().getString(stringId),
                getLocalPrice());
    }

    private static class ClientSideCustomData {
        private String localPrice;
        private @State int state;
    }
}