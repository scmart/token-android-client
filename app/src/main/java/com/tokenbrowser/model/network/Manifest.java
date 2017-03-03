package com.tokenbrowser.model.network;

import com.tokenbrowser.model.sofa.InitRequest;

import java.util.List;

public class Manifest {
    private String paymentAddress;
    private String ownerAddress;
    private String webApp;
    private String protocol;
    private String avatarUrl;
    private String displayName;
    private boolean featured;
    private InitRequest initRequest;
    private List<String> interfaces;
    private List<String> languages;

    public String getPaymentAddress() {
        return paymentAddress;
    }

    public String getOwnerAddress() {
        return ownerAddress;
    }

    public String getWebApp() {
        return webApp;
    }

    public String getProtocol() {
        return protocol;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isFeatured() {
        return featured;
    }

    public InitRequest getInitRequest() {
        return initRequest;
    }

    public List<String> getInterfaces() {
        return interfaces;
    }

    public List<String> getLanguages() {
        return languages;
    }
}
