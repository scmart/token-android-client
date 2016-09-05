package com.bakkenbaeck.toshi.crypto;

import android.content.SharedPreferences;
import android.util.Base64;

import com.bakkenbaeck.toshi.util.LogUtil;

import org.spongycastle.crypto.engines.AESFastEngine;
import org.spongycastle.crypto.modes.CBCBlockCipher;
import org.spongycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.spongycastle.crypto.params.KeyParameter;
import org.spongycastle.crypto.params.ParametersWithIV;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

public class Aes
{

    private static final String AES_KEY = "0";

    private final SharedPreferences preferences;
    private byte[] key;

    public Aes(final SharedPreferences preferences) {
        this.preferences = preferences;
        initAesKey();
    }

    /**
     * Encrypt the given plaintext bytes using the given key
     * @param data The plaintext to encrypt
     * @return The encrypted bytes
     */
    public byte[] encrypt(final byte[] data) {
        // 16 bytes is the IV size for AES256
        try
        {
            final PaddedBufferedBlockCipher cipher = new PaddedBufferedBlockCipher(new CBCBlockCipher(new AESFastEngine()));
            // Random iv
            final SecureRandom rng = new SecureRandom();
            final byte[] ivBytes = new byte[16];
            rng.nextBytes(ivBytes);

            cipher.init(true, new ParametersWithIV(new KeyParameter(this.key), ivBytes));
            final byte[] outBuf   = new byte[cipher.getOutputSize(data.length)];

            int processed = cipher.processBytes(data, 0, data.length, outBuf, 0);
            processed += cipher.doFinal(outBuf, processed);

            final byte[] outBuf2 = new byte[processed + 16];        // Make room for iv
            System.arraycopy(ivBytes, 0, outBuf2, 0, 16);    // Add iv
            System.arraycopy(outBuf, 0, outBuf2, 16, processed);    // Then the encrypted data

            return outBuf2;
        } catch(final Exception e) {
            LogUtil.e(getClass(), e.toString());
            throw new RuntimeException(e);
        }
    }

    /**
     * Decrypt the given data with the given key
     * @param data The data to decrypt
     * @return The decrypted bytes
     */
    public byte[] decrypt(final byte[] data) {
        try {
            final PaddedBufferedBlockCipher cipher = new PaddedBufferedBlockCipher(new CBCBlockCipher(new AESFastEngine()));
            final byte[] ivBytes = new byte[16];
            System.arraycopy(data, 0, ivBytes, 0, ivBytes.length); // Get iv from data
            final byte[] dataonly = new byte[data.length - ivBytes.length];
            System.arraycopy(data, ivBytes.length, dataonly, 0, data.length    - ivBytes.length);

            cipher.init(false, new ParametersWithIV(new KeyParameter(this.key), ivBytes));
            byte[] decrypted = new byte[cipher.getOutputSize(dataonly.length)];
            int len = cipher.processBytes(dataonly, 0, dataonly.length, decrypted,0);
            len += cipher.doFinal(decrypted, len);

            final byte[] trimmed = new byte[len];
            System.arraycopy(decrypted, 0, trimmed, 0, len);
            return trimmed;
        } catch(final Exception e) {
            LogUtil.e(getClass(), e.toString());
            throw new RuntimeException(e);
        }
    }

    private void initAesKey() {
        final byte[] aesKey = readKeyFromFile();
        if (aesKey == null) {
            this.key = generateKey();
            saveKeyToFile();
        } else {
            this.key = aesKey;
        }
    }

    private byte[] readKeyFromFile() {
        final String encoded = this.preferences.getString(AES_KEY, null);
        if (encoded == null) {
            return null;
        }
        return Base64.decode(encoded, Base64.NO_WRAP);
    }

    private void saveKeyToFile() {
        final String toWrite = Base64.encodeToString(this.key, Base64.NO_WRAP);
        this.preferences.edit().putString(AES_KEY, toWrite).apply();
    }

    /**
     * Generate a key suitable for AES256 encryption
     * @return The generated key
     * @throws NoSuchAlgorithmException
     */
    private byte[] generateKey() {
        try {
            final int outputKeyLength = 256;
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(outputKeyLength);
            final SecretKey key = keyGenerator.generateKey();
            return key.getEncoded();
        } catch (NoSuchAlgorithmException ex) {
            LogUtil.e(getClass(), ex.toString());
            throw new RuntimeException(ex);
        }
    }
}