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
