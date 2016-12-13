package com.bakkenbaeck.token.network.rest.model;


import java.util.List;

public class SentTransaction {
    private String tx_hash;
    private List<TokenError> errors;

    public List<TokenError> getErrors() {
        return this.errors;
    }
}
