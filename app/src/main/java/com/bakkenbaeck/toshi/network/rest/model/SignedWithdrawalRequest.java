package com.bakkenbaeck.toshi.network.rest.model;


public class SignedWithdrawalRequest {

    private final String type = "payment";
    private final SignedWithdrawalInternals payload;

    public SignedWithdrawalRequest(final String transaction, final String signature) {
        this.payload = new SignedWithdrawalInternals(transaction, signature);
    }

    private static class SignedWithdrawalInternals {
        private String transaction;
        private String signature;
        private SignedWithdrawalInternals(final String transaction, final String signature) {
            this.transaction = transaction;
            this.signature = signature;
        }
    }
}
