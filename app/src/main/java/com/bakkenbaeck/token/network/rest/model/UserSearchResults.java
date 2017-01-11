package com.bakkenbaeck.token.network.rest.model;


import com.bakkenbaeck.token.model.User;

import java.util.List;

public class UserSearchResults {
    private int limit;
    private int offset;
    private String query;
    private List<User> results;

    public List<User> getResults() {
        return this.results;
    }
}
