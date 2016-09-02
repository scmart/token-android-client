package com.bakkenbaeck.toshi.model;


import org.mindrot.jbcrypt.BCrypt;

public class WalletCredentials {

    private final String salt;
    private final String password;

    public WalletCredentials() {
        this(BCrypt.gensalt(16), BCrypt.gensalt(16));
    }

    public WalletCredentials(final String salt, final String password) {
        this.salt = salt;
        this.password = password;
    }

    public String getSalt() {
        return salt;
    }

    public String getPassword() {
        return password;
    }
}
