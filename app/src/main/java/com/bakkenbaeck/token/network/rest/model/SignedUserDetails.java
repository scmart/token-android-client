package com.bakkenbaeck.token.network.rest.model;


public class SignedUserDetails {

    private UserDetails payload;
    private String address;
    private String signature;

    public SignedUserDetails setEthAddress(final String address) {
        this.address = address;
        return this;
    }

    public SignedUserDetails setSignature(final String signature) {
        this.signature = signature;
        return this;
    }

    public SignedUserDetails setUserDetails(final UserDetails userDetails) {
        this.payload = userDetails;
        return this;
    }
}
