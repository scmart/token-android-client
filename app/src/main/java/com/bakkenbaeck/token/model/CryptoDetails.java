package com.bakkenbaeck.token.model;


public class CryptoDetails {

    private String eth_address;
    private String password_hash;
    private String encrypted_private_key;

    public CryptoDetails setEthAddress(final String eth_address) {
        this.eth_address = eth_address;
        return this;
    }

    public CryptoDetails setBCryptedPassword(final String password) {
        this.password_hash = password;
        return this;
    }

    public CryptoDetails setAesEncodedPrivateKey(final String private_key) {
        this.encrypted_private_key = private_key;
        return this;
    }
}
