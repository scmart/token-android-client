package com.bakkenbaeck.token.model.network;


import java.util.List;

public class SentTransaction {
    private String tx_hash;
    private List<TokenError> errors;

    public List<TokenError> getErrors() {
        return this.errors;
    }

    public String getTxHash() {
        return this.tx_hash;
    }
}
