package com.bakkenbaeck.toshi.manager;


import android.content.SharedPreferences;

import com.bakkenbaeck.toshi.crypto.Aes;
import com.bakkenbaeck.toshi.crypto.Wallet;
import com.bakkenbaeck.toshi.view.BaseApplication;
import com.securepreferences.SecurePreferences;

import org.mindrot.jbcrypt.BCrypt;

import java.security.Security;

public class WalletManager {

    // Ignore the values - they're for obfuscation purposes
    private static final String PRIVATE_KEY = "i";
    private static final String PASSWORD = "l";
    private SharedPreferences prefs;
    private Wallet wallet;
    private Aes aes;

    static {
        Security.insertProviderAt(new org.spongycastle.jce.provider.BouncyCastleProvider(), 1);
    }

    public WalletManager init() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                initAes();
                initWallet();
            }
        }).start();
        return this;
    }

    private void initAes() {
        this.prefs = new SecurePreferences(BaseApplication.get());
        this.aes = new Aes(this.prefs);
    }

    private void initWallet() {
        if (!walletExistsInPrefs()) {
            generateNewWallet();
        }
    }

    private boolean walletExistsInPrefs() {
        final String privateKey = getDecryptedPrivateKey();
        if (privateKey == null) {
            return false;
        }
        this.wallet = new Wallet().initFromPrivateKey(privateKey);
        return true;
    }

    private String getDecryptedPrivateKey() {
        final String encryptedPrivateKey = this.prefs.getString(PRIVATE_KEY, null);
        if (encryptedPrivateKey == null) {
            return null;
        }

        final String password = getStoredPassword();
        if(password == null) {
            throw new RuntimeException("Not implemented yet");
        }
        this.aes.initWithPassword(password);
        return aes.decrypt(encryptedPrivateKey);
    }

    private void generateNewWallet() {
        this.wallet = new Wallet().init();
        final String passwordGeneratedForUser = BCrypt.gensalt(16);
        this.aes.initWithPassword(passwordGeneratedForUser);

        final String encryptedPrivateKey = getEncryptedPrivateKey(this.wallet.getPrivateKey());
        this.prefs.edit()
                .putString(PRIVATE_KEY, encryptedPrivateKey)
                .putString(PASSWORD, passwordGeneratedForUser)
                .apply();
    }

    private String getEncryptedPrivateKey(final String privateKey) {
        return this.aes.encrypt(privateKey);
    }

    private String getStoredPassword() {
        return this.prefs.getString(PASSWORD, null);
    }

    public boolean shouldAskUserForPassword() {
        return getStoredPassword() == null;
    }
}
