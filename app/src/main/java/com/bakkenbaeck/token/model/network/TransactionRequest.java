package com.bakkenbaeck.token.model.network;


import com.bakkenbaeck.token.crypto.util.TypeConverter;
import com.bakkenbaeck.token.util.EthUtil;

import java.math.BigDecimal;
import java.math.BigInteger;

public class TransactionRequest {

    private String value;
    private String from;
    private String to;

    public TransactionRequest setValue(final BigDecimal ethAmount) {
        final BigInteger weiAmount = EthUtil.ethToWei(ethAmount);
        this.value = TypeConverter.toJsonHex(weiAmount);
        return this;
    }

    public TransactionRequest setToAddress(final String addressInHex) {
        this.to = addressInHex;
        return this;
    }

    public TransactionRequest setFromAddress(final String addressInHex) {
        this.from = addressInHex;
        return this;
    }
}
