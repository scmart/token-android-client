package com.bakkenbaeck.toshi.manager;


import android.content.SharedPreferences;
import android.util.Base64;

import com.bakkenbaeck.toshi.crypto.Aes;
import com.bakkenbaeck.toshi.crypto.Wallet;
import com.bakkenbaeck.toshi.model.WalletCredentials;
import com.bakkenbaeck.toshi.util.LogUtil;
import com.bakkenbaeck.toshi.view.BaseApplication;
import com.securepreferences.SecurePreferences;

import java.security.Security;

public class WalletManager {

    // Ignore the values - they're for obfuscation purposes
    private static final String PRIVATE_KEY = "i";
    private static final String SALT = "1";
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
        LogUtil.print(getClass(), this.wallet.toString());
        return true;
    }

    private String getDecryptedPrivateKey() {
        final String encryptedPrivateKey = this.prefs.getString(PRIVATE_KEY, null);
        if (encryptedPrivateKey == null) {
            return null;
        }

        final byte[] encryptedPkBytes = Base64.decode(encryptedPrivateKey, Base64.NO_WRAP);
        return new String(aes.decrypt(encryptedPkBytes));
    }

    private void generateNewWallet() {
        final WalletCredentials credentials = new WalletCredentials();
        this.wallet = new Wallet().init();
        LogUtil.print(getClass(), this.wallet.toString());
        final String encryptedPrivateKey = getEncryptedPrivateKey(this.wallet.getPrivateKey());
        this.prefs.edit()
                .putString(PRIVATE_KEY, encryptedPrivateKey)
                .putString(SALT, credentials.getSalt())
                .putString(PASSWORD, credentials.getPassword())
                .apply();
    }

    private String getEncryptedPrivateKey(final String privateKey) {
        final byte[] encryptedBytes = this.aes.encrypt(privateKey.getBytes());
        return Base64.encodeToString(encryptedBytes, Base64.NO_WRAP);
    }
}
