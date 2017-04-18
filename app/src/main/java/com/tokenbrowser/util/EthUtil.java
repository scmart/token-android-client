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

package com.tokenbrowser.util;


import com.tokenbrowser.crypto.util.TypeConverter;

import java.math.BigDecimal;
import java.math.BigInteger;

public class EthUtil {

    private static final int NUM_DECIMAL_PLACES = 4;
    private static final String USER_VISIBLE_STRING_FORMATTING = "%.4f";
    private static final BigDecimal weiToEthRatio = new BigDecimal("1000000000000000000");

    public static String hexAmountToUserVisibleString(final String hexEncodedWei) {
        final BigInteger wei = TypeConverter.StringHexToBigInteger(hexEncodedWei);
        return weiAmountToUserVisibleString(wei);
    }

    public static String weiAmountToUserVisibleString(final BigInteger wei) {
        final BigDecimal eth = weiToEth(wei);
        return ethAmountToUserVisibleString(eth);
    }

    public static String ethAmountToUserVisibleString(final BigDecimal eth) {
        return String.format(
                LocaleUtil.getLocale(),
                USER_VISIBLE_STRING_FORMATTING,
                eth.setScale(NUM_DECIMAL_PLACES, BigDecimal.ROUND_DOWN));
    }

    public static BigDecimal weiToEth(final BigInteger wei) {
        if (wei == null) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(wei)
                .divide(weiToEthRatio)
                .setScale(NUM_DECIMAL_PLACES, BigDecimal.ROUND_DOWN);
    }

    public static BigInteger ethToWei(final BigDecimal amountInEth) {
        return amountInEth.multiply(weiToEthRatio).toBigInteger();
    }

    public static String encodeToHex(final String value) throws NumberFormatException, NullPointerException {
        return String.format("%s%s", "0x", new BigInteger(value).toString(16));
    }
}
