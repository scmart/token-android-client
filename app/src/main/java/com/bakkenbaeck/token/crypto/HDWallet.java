package com.bakkenbaeck.token.crypto;

import android.content.Context;
import android.content.SharedPreferences;

import com.bakkenbaeck.token.R;
import com.bakkenbaeck.token.crypto.util.TypeConverter;
import com.bakkenbaeck.token.util.LogUtil;
import com.bakkenbaeck.token.view.BaseApplication;

import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.MnemonicCode;
import org.bitcoinj.wallet.DeterministicSeed;
import org.bitcoinj.wallet.KeyChain;
import org.bitcoinj.wallet.UnreadableWalletException;
import org.bitcoinj.wallet.Wallet;
import org.spongycastle.util.encoders.Hex;

import java.io.IOException;
import java.security.Security;

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

    public HDWallet init() {
        initPreferences();
        initWordList();
        initWallet();
        return this;
    }

    private void initPreferences() {
        this.prefs = BaseApplication.get().getSharedPreferences("wa", Context.MODE_PRIVATE);
    }

    private void initWordList() {
        try {
            MnemonicCode.INSTANCE = new MnemonicCode(BaseApplication.get().getResources().openRawResource(R.raw.bip39_wordlist), null);
        } catch (final IOException e) {
            LogUtil.e(getClass(), "Wordlist not loaded.");
            throw new RuntimeException(e);
        }
    }

    private HDWallet initWallet() {
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
            throw new RuntimeException("Unable to create wallet. Seed is invalid");
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

    public String signIdentity(final String data) {
        return sign(data.getBytes(), this.identityKey);
    }

    public String signTransaction(final String data) {
        return sign(data.getBytes(), this.receivingKey);
    }

    private String sign(final byte[] bytes, final ECKey key) {
        final byte[] msgHash = sha3(bytes);
        final ECKey.ECDSASignature signature = key.sign(msgHash);
        return signature.toHex();
    }

    public String getMasterSeed() {
        return this.masterSeed;
    }

    private String getPrivateKey() {
        return Hex.toHexString(this.identityKey.getPrivKeyBytes());
    }

    private String getPublicKey() {
        return Hex.toHexString(this.identityKey.getPubKey());
    }

    public String getAddress() {
        if(identityKey != null) {
            return TypeConverter.toJsonHex(this.identityKey.getAddress());
        }
        return null;
    }

    public String getWalletAddress() {
        if(this.receivingKey != null) {
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
