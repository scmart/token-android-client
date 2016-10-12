package com.bakkenbaeck.token.crypto.util;


import com.bakkenbaeck.token.crypto.cryptohash.Keccak256;

import org.spongycastle.util.Arrays;

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
}
