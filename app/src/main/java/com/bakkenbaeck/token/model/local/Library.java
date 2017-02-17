package com.bakkenbaeck.token.model.local;

public class Library {
    private String name;
    private String licence;

    public String getName() {
        return name;
    }

    public String getLicence() {
        return licence;
    }

    public Library setName(String name) {
        this.name = name;
        return this;
    }

    public Library setLicence(String licence) {
        this.licence = licence;
        return this;
    }
}
