package com.bakkenbaeck.token.model.sofa;


import com.bakkenbaeck.token.crypto.util.TypeConverter;

import java.math.BigInteger;

/**
 * PaymentRequest
 * <p>
 * Request an Ethereum transaction
 *
 */
public class PaymentRequest {

    /**
     * Value
     * <p>
     * Value of transaction in wei JSON-RPC encoded
     * (Required)
     *
     */
    private String value;
    /**
     * Destination Address
     * <p>
     * Ethereum address of recipient
     * (Required)
     *
     */
    private String destinationAddress;

    private ClientSideCustomData androidClientSideCustomData;

    public PaymentRequest setValue(final BigInteger value) {
        this.value = TypeConverter.toJsonHex(value);
        return this;
    }

    public PaymentRequest setDestinationAddress(final String destinationAddress) {
        this.destinationAddress = destinationAddress;
        return this;
    }

    public String getDestinationAddresss() {
        return this.destinationAddress;
    }

    public BigInteger getValue() {
        return TypeConverter.StringHexToBigInteger(this.value);
    }

    public PaymentRequest setLocalPrice(final String localPrice) {
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

    private static class ClientSideCustomData {
        private String localPrice;
    }
}