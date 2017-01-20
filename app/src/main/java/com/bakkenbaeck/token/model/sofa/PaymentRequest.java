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


    public PaymentRequest setValue(final BigInteger value) {
        this.value = TypeConverter.toJsonHex(value);
        return this;
    }

    public PaymentRequest setDestinationAddress(final String destinationAddress) {
        this.destinationAddress = destinationAddress;
        return this;
    }

    public BigInteger getValue() {
        return TypeConverter.StringHexToBigInteger(this.value);
    }
}