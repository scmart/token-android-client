package com.bakkenbaeck.token.network.rest.model;


import com.bakkenbaeck.token.crypto.util.TypeConverter;

import java.math.BigInteger;

public class TransactionRequest {

    private final String type = "transaction_create";
    private final WithdrawalInternals payload;

    public TransactionRequest(final BigInteger amountInWei, final String toHexAddress) {
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
