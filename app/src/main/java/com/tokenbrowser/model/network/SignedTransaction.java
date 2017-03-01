package com.tokenbrowser.model.network;


public class SignedTransaction {

    private String tx;
    private String signature;

    public SignedTransaction setEncodedTransaction(final String rlpEncodedTransaction) {
        this.tx = rlpEncodedTransaction;
        return this;
    }

    public SignedTransaction setSignature(final String signature) {
        this.signature = signature;
        return this;
    }
}
