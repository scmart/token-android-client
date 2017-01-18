package com.bakkenbaeck.token.model.sofa;


/**
 * TxRequest
 * <p>
 * Request an Ethereum transaction
 *
 */
public class TxRequest {

    /**
     * Currency
     * <p>
     * ISO 4217 currency code. Specifies the denomination of request value.
     * (Required)
     *
     */
    private String currency;
    /**
     * Value
     * <p>
     * Value of transaction in given currency (fractional numbers allowed)
     * (Required)
     *
     */
    private Double value;
    /**
     * Destination Address
     * <p>
     * Ethereum address of recipient
     * (Required)
     *
     */
    private String destinationAddress;
    /**
     * Sender Address
     * <p>
     * Ethereum address of sender
     * (Required)
     *
     */
    private String senderAddress;

    public TxRequest setCurrency(final String currency) {
        this.currency = currency;
        return this;
    }

    public TxRequest setValue(final Double value) {
        this.value = value;
        return this;
    }

    public TxRequest setDestinationAddress(final String destinationAddress) {
        this.destinationAddress = destinationAddress;
        return this;
    }

    public TxRequest setSenderAddress(final String senderAddress) {
        this.senderAddress = senderAddress;
        return this;
    }

    public String getCurrency() {
        return currency;
    }

    public Double getValue() {
        return value;
    }
}