package com.bakkenbaeck.token.model.network;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDetails {

    @JsonProperty
    private String username;

    @JsonProperty
    private CustomPayload custom;

    public UserDetails setWalletAddress(final String address) {
        initCustom();
        this.custom.payment_address = address;
        return this;
    }

    public UserDetails setUsername(final String username) {
        this.username = username;
        return this;
    }

    public UserDetails setAbout(final String about) {
        initCustom();
        this.custom.about = about;
        return this;
    }

    public UserDetails setLocation(final String location) {
        initCustom();
        this.custom.location = location;
        return this;
    }

    private void initCustom() {
        if (custom == null) {
            this.custom = new CustomPayload();
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private static class CustomPayload {
        @JsonProperty
        private String about;

        @JsonProperty
        private String location;

        @JsonProperty
        private String payment_address;
    }
}
