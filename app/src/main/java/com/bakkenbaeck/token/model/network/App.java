package com.bakkenbaeck.token.model.network;

import java.util.List;

public class App {
    private String displayName;
    private String protocol;
    private String webApp;
    private List<String> languages;
    private List<String> interfaces;
    private String avatarUrl;
    private String ethereumAddress;

    public String getDisplayName() {
        return displayName;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public List<String> getInterfaces() {
        return interfaces;
    }
}
