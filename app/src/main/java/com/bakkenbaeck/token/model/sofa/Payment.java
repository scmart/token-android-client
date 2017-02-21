package com.bakkenbaeck.token.model.sofa;

import com.bakkenbaeck.token.crypto.util.TypeConverter;
import com.bakkenbaeck.token.util.EthUtil;
import com.bakkenbaeck.token.view.BaseApplication;
import com.squareup.moshi.Json;

import java.math.BigDecimal;
import java.math.BigInteger;

public class Payment {

    private String value;
    private String toAddress;
    private String ownerAddress;
    private String txHash;
    private String status;

    @Json(name = SofaType.LOCAL_ONLY_PAYLOAD)
    private ClientSideCustomData androidClientSideCustomData;

    public Payment() {}

    public Payment setValue(final String value) {
        this.value = value;
        generateLocalPrice();
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

    public String getOwnerAddress() {
        return ownerAddress;
    }

    public Payment setOwnerAddress(final String ownerAddress) {
        this.ownerAddress = ownerAddress;
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


    private Payment setLocalPrice(final String localPrice) {
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

    public void generateLocalPrice() {
        final BigInteger weiAmount = TypeConverter.StringHexToBigInteger(this.value);
        final BigDecimal ethAmount = EthUtil.weiToEth(weiAmount);
        final String localAmount = BaseApplication.get().getTokenManager().getBalanceManager().convertEthToLocalCurrencyString(ethAmount);
        setLocalPrice(localAmount);
    }

    public String toUserVisibleString() {
        return String.format("%s %s", "Payment", getLocalPrice());
    }

    private static class ClientSideCustomData {
        private String localPrice;
    }
}
