package com.tokenbrowser.model.network;

import java.util.List;

public class Apps {
    private int offset;
    private String query;
    private int limit;
    private List<App> results;

    public List<App> getApps() {
        return results;
    }
}
