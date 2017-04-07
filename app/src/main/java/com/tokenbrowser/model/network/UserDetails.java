package com.tokenbrowser.model.network;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDetails {

    @JsonProperty
    private String username;

    @JsonProperty
    private String payment_address;

    @JsonProperty
    private String about;

    @JsonProperty
    private String location;

    @JsonProperty
    private String name;

    public UserDetails setUsername(final String username) {
        this.username = username;
        return this;
    }

    public UserDetails setPaymentAddress(final String address) {
        this.payment_address = address;
        return this;
    }

    public UserDetails setAbout(final String about) {
        this.about = about;
        return this;
    }

    public UserDetails setLocation(final String location) {
        this.location = location;
        return this;
    }

    public UserDetails setDisplayName(final String name) {
        this.name = name;
        return this;
    }
}
