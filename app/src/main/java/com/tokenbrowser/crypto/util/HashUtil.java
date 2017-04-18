/*
 * 	Copyright (c) 2017. Token Browser, Inc
 *
 * 	This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
