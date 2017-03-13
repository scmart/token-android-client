package com.tokenbrowser.util;


import com.tokenbrowser.crypto.util.TypeConverter;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DecimalFormat;

public class EthUtil {

    private static final BigDecimal weiToEthRatio = new BigDecimal("1000000000000000000");
    private static final DecimalFormat formatting = new DecimalFormat("#0.##########");


    public static String hexAmountToUserVisibleString(final String hexEncodedWei) {
        final BigInteger wei = TypeConverter.StringHexToBigInteger(hexEncodedWei);
        return weiAmountToUserVisibleString(wei);
    }

    public static String weiAmountToUserVisibleString(final BigInteger wei) {
        final BigDecimal eth = weiToEth(wei);
        return ethAmountToUserVisibleString(eth);
    }

    public static String ethAmountToUserVisibleString(final BigDecimal eth) {
        return String.format(LocaleUtil.getLocale(), "%.4f", eth.setScale(4, BigDecimal.ROUND_DOWN));
    }

    public static BigDecimal weiToEth(final BigInteger wei) {
        if (wei == null) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(wei).divide(weiToEthRatio);
    }

    public static BigInteger ethToWei(final BigDecimal amountInEth) {
        return amountInEth.multiply(weiToEthRatio).toBigInteger();
    }
}
