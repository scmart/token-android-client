package com.bakkenbaeck.toshi.manager;


import android.content.SharedPreferences;

import com.bakkenbaeck.toshi.util.LogUtil;
import com.bakkenbaeck.toshi.view.BaseApplication;
import com.google.common.base.Joiner;
import com.securepreferences.SecurePreferences;

import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.wallet.DeterministicSeed;
import org.bitcoinj.wallet.UnreadableWalletException;
import org.bitcoinj.wallet.Wallet;

public class WalletManager {

    private static final String SEED_WORDS = "seed_words";
    private static final String SEED_BIRTHDAY = "seed_birthday";
    private SharedPreferences prefs;
    private Wallet wallet;

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
    }

    private void generateNewWallet() {
        this.wallet = new Wallet(MainNetParams.get());

        final DeterministicSeed seed = wallet.getKeyChainSeed();
        this.prefs.edit()
                .putString(SEED_WORDS, Joiner.on(" ").join(seed.getMnemonicCode()))
                .putLong(SEED_BIRTHDAY, seed.getCreationTimeSeconds())
                .apply();
    }

    private boolean walletExistsInPrefs() {
        final String seedWords = this.prefs.getString(SEED_WORDS, null);
        final long seedBirthday = this.prefs.getLong(SEED_BIRTHDAY, 0);
        if (seedWords == null || seedBirthday == 0) {
            return false;
        }

        try {
            final DeterministicSeed seed = new DeterministicSeed(seedWords, null, "", seedBirthday);
            this.wallet = Wallet.fromSeed(MainNetParams.get(), seed);
        } catch (final UnreadableWalletException e) {
            LogUtil.e(getClass(), e.toString());
            return false;
        }

        return true;
    }
}
