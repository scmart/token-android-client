package com.tokenbrowser.model.network;


import com.tokenbrowser.model.local.User;

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
