package com.tokenbrowser.crypto.util;


import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;

public class TypeConverter {

    public static BigInteger StringHexToBigInteger(final String input) {
        if (input == null) {
            return BigInteger.ZERO;
        }

        final String hexa = input.startsWith("0x") ? input.substring(2) : input;
        try {
            return new BigInteger(hexa, 16);
        } catch (final NumberFormatException ex) {
            return BigInteger.ZERO;
        }
    }

    public static byte[] StringHexToByteArray(String x) throws Exception {
        if (x.startsWith("0x")) {
            x = x.substring(2);
        }
        if (x.length() % 2 != 0) x = "0" + x;
        return Hex.decode(x);
    }

    public static String toJsonHex(final byte[] x) {
        return "0x"+Hex.toHexString(x);
    }

    public static String toJsonHex(final String x) {
        return "0x"+x;
    }

    public static String toJsonHex(final long n) {
        return "0x"+Long.toHexString(n);
    }

    public static String toJsonHex(final BigInteger n) {
        return "0x"+ n.toString(16);
    }
}
