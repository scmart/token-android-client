package com.bakkenbaeck.toshi.crypto;

import android.content.SharedPreferences;

import com.bakkenbaeck.toshi.util.LogUtil;
import com.bakkenbaeck.toshi.view.BaseApplication;
import com.securepreferences.SecurePreferences;

import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.security.Security;

import static com.bakkenbaeck.toshi.crypto.util.HashUtil.sha3;

public class Wallet {

    static {
        Security.insertProviderAt(new org.spongycastle.jce.provider.BouncyCastleProvider(), 1);
    }

    private static final String PRIVATE_KEY = "i";
    private SharedPreferences prefs;
    private Aes aes;
    private ECKey ecKey;

    public Wallet() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                initAes();
            }
        }).start();
    }

    public Wallet initWallet(final String password) {
        final String encryptedPrivateKey = getEncryptedPrivateKey();
        if (encryptedPrivateKey == null) {
            return generateNewWallet(password);
        }

        final String privateKey = decryptPrivateKey(encryptedPrivateKey, password);
        return initFromPrivateKey(privateKey);
    }

    private void initAes() {
        this.prefs = new SecurePreferences(BaseApplication.get());
        this.aes = new Aes(this.prefs);
    }

    public String sign(final String message) {
        try {
            final byte[] msgHash= sha3(message.getBytes());
            final ECKey.ECDSASignature signature = this.ecKey.sign(msgHash);
            return signature.toHex();
        } catch (final Exception e) {
            LogUtil.error(getClass(), e.toString());
        }
        return null;
    }

    public String getPrivateKey() {
        return Hex.toHexString(this.ecKey.getPrivKeyBytes());
    }

    private String getPublicKey() {
        return Hex.toHexString(this.ecKey.getPubKey());
    }

    private String getAddress() {
        return Hex.toHexString(this.ecKey.getAddress());
    }

    @Override
    public String toString() {
        return "Private: " + getPrivateKey() + "\nPublic: " + getPublicKey() + "\nAddress: " + getAddress();
    }

    private Wallet generateNewWallet(final String password) {
        this.ecKey = new ECKey();
        final String encryptedPrivateKey = encryptPrivateKey(getPrivateKey(), password);
        this.prefs.edit()
                .putString(PRIVATE_KEY, encryptedPrivateKey)
                .apply();
        return this;
    }

    private Wallet initFromPrivateKey(final String privateKey) {
        final BigInteger privKey = new BigInteger(1, Hex.decode(privateKey));
        this.ecKey = ECKey.fromPrivate(privKey);
        return this;
    }


    private String getEncryptedPrivateKey() {
        final String encryptedPrivateKey = this.prefs.getString(PRIVATE_KEY, null);
        if (encryptedPrivateKey == null) {
            return null;
        }
        return encryptedPrivateKey;
    }

    private String decryptPrivateKey(final String encryptedPrivateKey, final String password) {
        return aes.decrypt(encryptedPrivateKey, password);
    }

    private String encryptPrivateKey(final String privateKey, final String password) {
        return this.aes.encrypt(privateKey, password);
    }
}
