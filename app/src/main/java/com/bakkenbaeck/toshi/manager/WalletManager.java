package com.bakkenbaeck.toshi.manager;


import android.content.SharedPreferences;

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

    static {
        Security.insertProviderAt(new org.spongycastle.jce.provider.BouncyCastleProvider(), 1);
    }

    public WalletManager init() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                initWallet();
            }
        }).start();
        return this;
    }

    private void initWallet() {
        this.prefs = new SecurePreferences(BaseApplication.get());
        if (!walletExistsInPrefs()) {
            generateNewWallet();
        }
        LogUtil.print(getClass(), this.wallet.toString());
    }

    private boolean walletExistsInPrefs() {
        final String privateKey = this.prefs.getString(PRIVATE_KEY, null);
        if (privateKey == null) {
            return false;
        }
        this.wallet = new Wallet().initFromPrivateKey(privateKey);

        return true;
    }

    private void generateNewWallet() {
        final WalletCredentials credentials = new WalletCredentials();
        this.wallet = new Wallet().init();
        this.prefs.edit()
                .putString(PRIVATE_KEY, this.wallet.getPrivateKey())
                .putString(SALT, credentials.getSalt())
                .putString(PASSWORD, credentials.getPassword())
                .apply();
    }
}
