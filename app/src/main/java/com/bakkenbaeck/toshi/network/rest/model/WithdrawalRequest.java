package com.bakkenbaeck.toshi.network.rest.model;


import com.bakkenbaeck.toshi.crypto.util.TypeConverter;

import java.math.BigInteger;

public class WithdrawalRequest {

    private final String type = "transaction_create";
    private final WithdrawalInternals payload;

    public WithdrawalRequest(final BigInteger amountInWei, final String toHexAddress) {
        this.payload = new WithdrawalInternals(amountInWei, toHexAddress);
    }

    private static class WithdrawalInternals {
        private String amount;
        private String to;
        private WithdrawalInternals(final BigInteger amountInWei, final String toHexAddress) {
            this.amount = TypeConverter.toJsonHex(amountInWei.toByteArray());
            this.to = toHexAddress.trim();
        }
    }
}
