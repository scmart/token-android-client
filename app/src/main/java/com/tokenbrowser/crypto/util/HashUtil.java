package com.tokenbrowser.crypto.util;


import com.tokenbrowser.crypto.cryptohash.Keccak256;

import org.spongycastle.util.Arrays;
import org.whispersystems.signalservice.internal.util.Base64;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class HashUtil {

    public static byte[] sha3omit12(byte[] input) {
        byte[] hash = sha3(input);
        return Arrays.copyOfRange(hash, 12, hash.length);
    }

    public static byte[] sha3(byte[] input) {
        Keccak256 digest = new Keccak256();
        digest.update(input);
        return digest.digest();
    }

    public static String getSecret(final int size) {
        byte[] secret = getSecretBytes(size);
        return Base64.encodeBytes(secret);
    }

    private static byte[] getSecretBytes(final int size) {
        byte[] secret = new byte[size];
        getSecureRandom().nextBytes(secret);
        return secret;
    }

    private static SecureRandom getSecureRandom() {
        try {
            return SecureRandom.getInstance("SHA1PRNG");
        } catch (final NoSuchAlgorithmException e) {
            throw new AssertionError(e);
        }
    }
}
