package com.tokenbrowser.crypto;

import android.content.Context;
import android.content.SharedPreferences;

import com.tokenbrowser.crypto.util.TypeConverter;
import com.tokenbrowser.R;
import com.tokenbrowser.exception.InvalidMasterSeedException;
import com.tokenbrowser.util.FileNames;
import com.tokenbrowser.util.LogUtil;
import com.tokenbrowser.view.BaseApplication;

import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.MnemonicCode;
import org.bitcoinj.crypto.MnemonicException;
import org.bitcoinj.wallet.DeterministicSeed;
import org.bitcoinj.wallet.KeyChain;
import org.bitcoinj.wallet.UnreadableWalletException;
import org.bitcoinj.wallet.Wallet;
import org.spongycastle.util.encoders.Hex;

import java.io.IOException;
import java.security.Security;

import rx.Single;

import static com.tokenbrowser.crypto.util.HashUtil.sha3;

public class HDWallet {

    static {
        Security.insertProviderAt(new org.spongycastle.jce.provider.BouncyCastleProvider(), 1);
    }

    private static final String MASTER_SEED = "ms";
    private SharedPreferences prefs;
    private ECKey identityKey;
    private ECKey receivingKey;
    private String masterSeed;

    private void tryInit() {
        if (this.prefs != null) return;
        initPreferences();
        initWordList();
    }

    private void initPreferences() {
        this.prefs = BaseApplication.get().getSharedPreferences(FileNames.WALLET_PREFS, Context.MODE_PRIVATE);
    }

    private void initWordList() {
        try {
            MnemonicCode.INSTANCE = new MnemonicCode(BaseApplication.get().getResources().openRawResource(R.raw.bip39_wordlist), null);
        } catch (final IOException e) {
            LogUtil.e(getClass(), "Wordlist not loaded.");
            throw new RuntimeException(e);
        }
    }

    public Single<HDWallet> getExistingWallet() {
        return Single.fromCallable(() -> {
            tryInit();
            this.masterSeed = readMasterSeedFromStorage();
            if (this.masterSeed == null) throw new InvalidMasterSeedException(new Throwable("Master seed is null"));
            final Wallet wallet = initFromMasterSeed(this.masterSeed);
            deriveKeysFromWallet(wallet);

            return this;
        });
    }

    public Single<HDWallet> getOrCreateWallet() {
        return Single.fromCallable(() -> {
            tryInit();
            this.masterSeed = readMasterSeedFromStorage();
            final Wallet wallet = this.masterSeed == null
                    ? generateNewWallet()
                    : initFromMasterSeed(this.masterSeed);

            deriveKeysFromWallet(wallet);

            return this;
        });
    }

    private Wallet generateNewWallet() {
        final Wallet wallet = new Wallet(NetworkParameters.fromID(NetworkParameters.ID_MAINNET));
        final DeterministicSeed seed = wallet.getKeyChainSeed();
        final String masterSeed = seedToString(seed);
        saveMasterSeedToStorage(masterSeed);

        return wallet;
    }

    private Wallet initFromMasterSeed(final String masterSeed) {
        try {
            final DeterministicSeed seed = getSeed(masterSeed);
            seed.check();
            return Wallet.fromSeed(getNetworkParameters(), seed);
        } catch (final UnreadableWalletException | MnemonicException e) {
            throw new RuntimeException("Unable to create wallet. Seed is invalid");
        }
    }

    public Single<HDWallet> createFromMasterSeed(final String masterSeed) {
        return Single.fromCallable(() -> {
            tryInit();
            try {
                final DeterministicSeed seed = getSeed(masterSeed);
                seed.check();
                final Wallet wallet = Wallet.fromSeed(getNetworkParameters(), seed);
                deriveKeysFromWallet(wallet);
                saveMasterSeedToStorage(masterSeed);
                return this;
            } catch (final UnreadableWalletException | MnemonicException e) {
                throw new InvalidMasterSeedException(e);
            }
        });
    }

    private DeterministicSeed getSeed(final String masterSeed) throws UnreadableWalletException {
        return new DeterministicSeed(masterSeed, null, "", 0);
    }

    private NetworkParameters getNetworkParameters() {
        return NetworkParameters.fromID(NetworkParameters.ID_MAINNET);
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

        if (key == null) {
            throw new IOException("Unable to derive key");
        }

        return ECKey.fromPrivate(key.getPrivKey());
    }

    public String signIdentity(final String data) {
        return sign(data.getBytes(), this.identityKey);
    }

    public String signTransaction(final String data) {
        try {
            final byte[] transactionBytes = TypeConverter.StringHexToByteArray(data);
            return sign(transactionBytes, this.receivingKey);
        } catch (final Exception e) {
            LogUtil.print(getClass(), "Unable to sign transaction. " + e);
            return null;
        }
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
        if(this.identityKey != null) {
            final byte[] privateKeyByes = this.identityKey.getPrivKeyBytes();
            return privateKeyByes != null ? Hex.toHexString(privateKeyByes) : null;
        }
        return null;
    }

    private String getPublicKey() {
        if (this.identityKey == null) return null;
        return Hex.toHexString(this.identityKey.getPubKey());
    }

    public String getOwnerAddress() {
        if (this.identityKey == null) return null;
        return TypeConverter.toJsonHex(this.identityKey.getAddress());
    }

    public String getPaymentAddress() {
        if(this.receivingKey != null) {
            return TypeConverter.toJsonHex(this.receivingKey.getAddress());
        }
        return null;
    }

    @Override
    public String toString() {
        return "Private: " + getPrivateKey() + "\nPublic: " + getPublicKey() + "\nAddress: " + getOwnerAddress();
    }

    private void saveMasterSeedToStorage(final String masterSeed) {
        this.prefs.edit()
                .putString(MASTER_SEED, masterSeed)
                .apply();
        this.masterSeed = masterSeed;
    }

    private String readMasterSeedFromStorage() {
        return this.prefs.getString(MASTER_SEED, null);
    }

    public void clear() {
        this.prefs
                .edit()
                .clear()
                .apply();
    }

    private String seedToString(final DeterministicSeed seed) {
        if (seed == null || seed.getMnemonicCode() == null) {
            return null;
        }

        final StringBuilder sb = new StringBuilder();
        for (final String word : seed.getMnemonicCode()) {
            sb.append(word).append(" ");
        }

        // Remove the extraneous space character
        sb.deleteCharAt(sb.length() - 1);

        return sb.toString();
    }
}
