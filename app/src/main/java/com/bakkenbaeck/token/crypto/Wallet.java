package com.bakkenbaeck.token.crypto;

import android.content.Context;
import android.content.SharedPreferences;

import com.bakkenbaeck.token.crypto.util.TypeConverter;
import com.bakkenbaeck.token.util.LogUtil;
import com.bakkenbaeck.token.view.BaseApplication;
import com.google.android.gms.tasks.RuntimeExecutionException;

import org.mindrot.jbcrypt.BCrypt;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.security.Security;

import rx.Observable;
import rx.Subscriber;

import static com.bakkenbaeck.token.crypto.util.HashUtil.sha3;

public class Wallet {

    static {
        Security.insertProviderAt(new org.spongycastle.jce.provider.BouncyCastleProvider(), 1);
    }

    private static final String PRIVATE_KEY = "i";
    private SharedPreferences prefs;
    private Aes aes;
    private ECKey ecKey;
    private String encryptedPrivateKey;
    private String bCryptedPassword;


    public Wallet() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                initAes();
            }
        }).start();
    }

    public Observable<Wallet> initWallet(final String password, final String salt) {
        return Observable.create(new Observable.OnSubscribe<Wallet>() {
            @Override
            public void call(final Subscriber<? super Wallet> subscriber) {
                subscriber.onNext(initWalletSync(password, salt));
                subscriber.onCompleted();
            }
        });
    }

    private Wallet initWalletSync(final String password, final String salt) {
        this.bCryptedPassword = bCryptPassword(password, salt);
        this.encryptedPrivateKey = readEncryptedPrivateKeyFromStorage();
        if (this.encryptedPrivateKey == null) {
            return generateNewWallet(password);
        }

        final String privateKey = decryptPrivateKey(this.encryptedPrivateKey, password);
        return initFromPrivateKey(privateKey);
    }

    private void initAes() {
        this.prefs = BaseApplication.get().getSharedPreferences("wa", Context.MODE_PRIVATE);
        this.aes = new Aes();
    }

    public String sign(final String hexString) {
        try {
            final byte[] msgHash = sha3(TypeConverter.StringHexToByteArray(hexString));
            final ECKey.ECDSASignature signature = this.ecKey.sign(msgHash);
            return signature.toHex();
        } catch (final Exception e) {
            LogUtil.error(getClass(), e.toString());
        }
        return null;
    }

    public String getEncryptedPrivateKey() {
        return this.encryptedPrivateKey;
    }
    private String getPrivateKey() {
        return Hex.toHexString(this.ecKey.getPrivKeyBytes());
    }

    private String getPublicKey() {
        return Hex.toHexString(this.ecKey.getPubKey());
    }

    public String getAddress() {
        return TypeConverter.toJsonHex(this.ecKey.getAddress());
    }

    @Override
    public String toString() {
        return "Private: " + getPrivateKey() + "\nPublic: " + getPublicKey() + "\nAddress: " + getAddress();
    }

    private Wallet generateNewWallet(final String password) {
        this.ecKey = new ECKey();
        this.encryptedPrivateKey = encryptPrivateKey(getPrivateKey(), password);
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


    private String readEncryptedPrivateKeyFromStorage() {
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

    private String bCryptPassword(final String password, final String salt) {
        if (salt == null) {
            throw new RuntimeExecutionException(new IllegalStateException("No salt found in preferences"));
        }
        return BCrypt.hashpw(password, salt);
    }

    public String getBCryptedPassword() {
        return this.bCryptedPassword;
    }
}
