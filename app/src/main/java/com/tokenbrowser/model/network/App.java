package com.tokenbrowser.model.network;

import com.tokenbrowser.model.sofa.InitRequest;

import java.util.List;

public class App {
    private List<String> languages;
    private String paymentAddress;
    private String webApp;
    private String displayName;
    private List<String> interfaces;
    private String protocol;
    private String avatarUrl;
    private String ownerAddress;
    private InitRequest initRequest;

    public List<String> getLanguages() {
        return languages;
    }

    public String getPaymentAddress() {
        return paymentAddress;
    }

    public String getWebApp() {
        return webApp;
    }

    public String getDisplayName() {
        return displayName;
    }

    public List<String> getInterfaces() {
        return interfaces;
    }

    public String getProtocol() {
        return protocol;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public String getOwnerAddress() {
        return ownerAddress;
    }

    public InitRequest getInitRequest() {
        return initRequest;
    }
}
