package com.bakkenbaeck.token.network.rest.model;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDetails {

    @JsonProperty
    private String username;

    @JsonProperty
    private long timestamp;

    public UserDetails setTimestamp(final long timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public UserDetails setUsername(final String username) {
        this.username = username;
        return this;
    }
}
