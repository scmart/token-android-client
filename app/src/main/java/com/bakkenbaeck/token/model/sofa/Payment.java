package com.bakkenbaeck.token.model.sofa;


import com.bakkenbaeck.token.crypto.util.TypeConverter;
import com.bakkenbaeck.token.model.local.Transaction;
import com.bakkenbaeck.token.util.EthUtil;
import com.bakkenbaeck.token.view.BaseApplication;
import com.squareup.moshi.Json;

import java.math.BigDecimal;
import java.math.BigInteger;

public class Payment {

    private String txHash;
    private String value;

    @Json(name = SofaType.LOCAL_ONLY_PAYLOAD)
    private ClientSideCustomData androidClientSideCustomData;

    public Payment() {}

    public Payment(final Transaction transaction) {
        setValue(transaction.getEthAmount());
    }

    public Payment setTxHash(final String txHash) {
        this.txHash = txHash;
        return this;
    }

    public Payment setValue(final BigDecimal ethAmount) {
        final BigInteger weiAmount = EthUtil.ethToWei(ethAmount);
        this.value = TypeConverter.toJsonHex(weiAmount);
        generateLocalPrice();
        return this;
    }

    public BigInteger getValue() {
        return TypeConverter.StringHexToBigInteger(this.value);
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
        final String localAmount = BaseApplication.get().getTokenManager().getBalanceManager().getMarketRateInLocalCurrency(ethAmount);
        setLocalPrice(localAmount);
    }

    private static class ClientSideCustomData {
        private String localPrice;
    }
}
