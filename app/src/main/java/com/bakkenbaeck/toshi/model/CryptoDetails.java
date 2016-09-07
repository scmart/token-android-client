package com.bakkenbaeck.toshi.model;


public class CryptoDetails {

    private String eth_address;
    private String password;
    private String private_key;

    public CryptoDetails setEthAddress(final String eth_address) {
        this.eth_address = eth_address;
        return this;
    }

    public CryptoDetails setBCryptedPassword(final String password) {
        this.password = password;
        return this;
    }

    public CryptoDetails setAesEncodedPrivateKey(final String private_key) {
        this.private_key = private_key;
        return this;
    }
}
