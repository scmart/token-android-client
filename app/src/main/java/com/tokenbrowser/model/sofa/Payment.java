package com.tokenbrowser.model.sofa;

import android.support.annotation.IntDef;

import com.squareup.moshi.Json;
import com.tokenbrowser.R;
import com.tokenbrowser.crypto.HDWallet;
import com.tokenbrowser.crypto.util.TypeConverter;
import com.tokenbrowser.util.EthUtil;
import com.tokenbrowser.util.LocaleUtil;
import com.tokenbrowser.view.BaseApplication;

import java.math.BigDecimal;
import java.math.BigInteger;

import rx.Single;
import rx.schedulers.Schedulers;

public class Payment {

    private String value;
    private String toAddress;
    private String fromAddress;
    private String txHash;
    private String status;

    @IntDef({
            NOT_RELEVANT,
            TO_LOCAL_USER,
            FROM_LOCAL_USER,
    })
    public @interface PaymentDirection {}
    public static final int NOT_RELEVANT = 0;
    public static final int TO_LOCAL_USER = 1;
    public static final int FROM_LOCAL_USER = 2;

    @Json(name = SofaType.LOCAL_ONLY_PAYLOAD)
    private ClientSideCustomData androidClientSideCustomData;

    public Payment() {}

    public Payment setValue(final String value) {
        this.value = value;
        return this;
    }

    public String getValue() {
        return this.value;
    }

    public String getToAddress() {
        return this.toAddress;
    }

    public Payment setToAddress(final String toAddress) {
        this.toAddress = toAddress;
        return this;
    }

    public String getFromAddress() {
        return fromAddress;
    }

    public Payment setFromAddress(final String ownerAddress) {
        this.fromAddress = ownerAddress;
        return this;
    }

    public String getTxHash() {
        return this.txHash;
    }

    public Payment setTxHash(final String txHash) {
        this.txHash = txHash;
        return this;
    }

    public String getStatus() {
        return this.status;
    }

    public Payment setStatus(final String status) {
        this.status = status;
        return this;
    }


    public Payment setLocalPrice(final String localPrice) {
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

    public Single<Payment> generateLocalPrice() {
        final BigInteger weiAmount = TypeConverter.StringHexToBigInteger(this.value);
        final BigDecimal ethAmount = EthUtil.weiToEth(weiAmount);
        return BaseApplication
                .get()
                .getTokenManager()
                .getBalanceManager()
                .convertEthToLocalCurrencyString(ethAmount)
                .map(this::setLocalPrice);
    }

    public String toUserVisibleString(final boolean sentByLocal) {
        final int stringId = sentByLocal
                ? R.string.latest_message__payment_outgoing
                : R.string.latest_message__payment_incoming;
        return String.format(
                LocaleUtil.getLocale(),
                BaseApplication.get().getResources().getString(stringId),
                getLocalPrice());
    }

    public Single<Integer> getPaymentDirection() {
        return BaseApplication
                .get()
                .getTokenManager()
                .getWallet()
                .toObservable()
                .map(this::getPaymentDirection)
                .toSingle()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io());
    }

    private @PaymentDirection int getPaymentDirection(final HDWallet hdWallet) {
        if (toAddress.equals(hdWallet.getPaymentAddress())) {
            return TO_LOCAL_USER;
        }

        if (fromAddress.equals(hdWallet.getPaymentAddress())) {
            return FROM_LOCAL_USER;
        }

        return NOT_RELEVANT;
    }


    private static class ClientSideCustomData {
        private String localPrice;
    }
}
