package com.bakkenbaeck.token.crypto;

import android.content.Context;
import android.content.SharedPreferences;

import com.bakkenbaeck.token.crypto.util.TypeConverter;
import com.bakkenbaeck.token.util.LogUtil;
import com.bakkenbaeck.token.view.BaseApplication;

import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.wallet.DeterministicSeed;
import org.bitcoinj.wallet.KeyChain;
import org.bitcoinj.wallet.UnreadableWalletException;
import org.bitcoinj.wallet.Wallet;
import org.spongycastle.util.encoders.Hex;

import java.io.IOException;
import java.security.Security;

import rx.Observable;
import rx.Subscriber;

import static com.bakkenbaeck.token.crypto.util.HashUtil.sha3;

public class HDWallet {

    static {
        Security.insertProviderAt(new org.spongycastle.jce.provider.BouncyCastleProvider(), 1);
    }

    private static final String MASTER_SEED = "ms";
    private SharedPreferences prefs;
    private ECKey identityKey;
    private ECKey receivingKey;
    private String masterSeed;


    public HDWallet() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                initPreferences();
            }
        }).start();
    }

    private void initPreferences() {
        this.prefs = BaseApplication.get().getSharedPreferences("wa", Context.MODE_PRIVATE);
    }

    public Observable<HDWallet> initWallet() {
        return Observable.create(new Observable.OnSubscribe<HDWallet>() {
            @Override
            public void call(final Subscriber<? super HDWallet> subscriber) {
                subscriber.onNext(initWalletSync());
                subscriber.onCompleted();
            }
        });
    }

    private HDWallet initWalletSync() {
        this.masterSeed = readMasterSeedFromStorage();
        final Wallet wallet = this.masterSeed == null
                ? generateNewWallet()
                : initFromMasterSeed(masterSeed);

        deriveKeysFromWallet(wallet);

        return this;
    }

    private Wallet generateNewWallet() {
        final Wallet wallet = new Wallet(NetworkParameters.fromID(NetworkParameters.ID_MAINNET));
        final DeterministicSeed seed = wallet.getKeyChainSeed();
        this.masterSeed = seedToString(seed);
        this.prefs.edit()
                .putString(MASTER_SEED, this.masterSeed)
                .apply();

        return wallet;
    }

    private Wallet initFromMasterSeed(final String masterSeed) {
        try {
            final NetworkParameters networkParameters = NetworkParameters.fromID(NetworkParameters.ID_MAINNET);
            final DeterministicSeed seed = new DeterministicSeed(masterSeed, null, "", 0);
            return Wallet.fromSeed(networkParameters, seed);
        } catch (final UnreadableWalletException e) {
            throw new RuntimeException("Unable to crate wallet. Seed is invalid");
        }
    }

    private void deriveKeysFromWallet(final Wallet wallet) {
        try {
            deriveIdentityKey(wallet);
            deriveReceivingKey(wallet);
        } catch (final UnreadableWalletException | IOException ex) {
            throw new RuntimeException("Error deriving keys: " + ex);
        }
    }

    private void deriveIdentityKey(final Wallet wallet) throws IOException, UnreadableWalletException {
        this.identityKey = deriveKeyFromWallet(wallet, 0, KeyChain.KeyPurpose.AUTHENTICATION);
    }

    private void deriveReceivingKey(final Wallet wallet) throws IOException, UnreadableWalletException {
        this.receivingKey = deriveKeyFromWallet(wallet, 0, KeyChain.KeyPurpose.RECEIVE_FUNDS);
    }

    private ECKey deriveKeyFromWallet(final Wallet wallet, final int iteration, final KeyChain.KeyPurpose keyPurpose) throws UnreadableWalletException, IOException {
        DeterministicKey key = null;
        for (int i = 0; i <= iteration; i++) {
            key = wallet.freshKey(keyPurpose);
        }
        return ECKey.fromPrivate(key.getPrivKey());
    }

    public String signHexString(final String hexString) {
        try {
            return sign(TypeConverter.StringHexToByteArray(hexString));
        } catch (final Exception ex) {
            LogUtil.e(getClass(), "Unable to sign. " + ex);
        }
        return null;
    }

    public String signString(final String data) {
        return sign(data.getBytes());
    }

    public String sign(final byte[] bytes) {
        final byte[] msgHash = sha3(bytes);
        final ECKey.ECDSASignature signature = this.receivingKey.sign(msgHash);
        return signature.toHex();
    }

    public String getMasterSeed() {
        return this.masterSeed;
    }

    private String getPrivateKey() {
        return Hex.toHexString(this.receivingKey.getPrivKeyBytes());
    }

    private String getPublicKey() {
        return Hex.toHexString(this.receivingKey.getPubKey());
    }

    public String getAddress() {
        if(receivingKey != null) {
            return TypeConverter.toJsonHex(this.receivingKey.getAddress());
        }

        return null;
    }

    @Override
    public String toString() {
        return "Private: " + getPrivateKey() + "\nPublic: " + getPublicKey() + "\nAddress: " + getAddress();
    }

    private String readMasterSeedFromStorage() {
        return this.prefs.getString(MASTER_SEED, null);
    }

    private String seedToString(final DeterministicSeed seed) {
        final StringBuilder sb = new StringBuilder();
        for (final String word : seed.getMnemonicCode()) {
            sb.append(word).append(" ");
        }

        // Remove the extraneous space character
        sb.deleteCharAt(sb.length() - 1);

        return sb.toString();
    }
}
