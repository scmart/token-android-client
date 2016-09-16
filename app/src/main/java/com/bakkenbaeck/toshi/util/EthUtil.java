package com.bakkenbaeck.toshi.util;


import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DecimalFormat;

public class EthUtil {

    private static final BigDecimal weiToEthRatio = new BigDecimal("1000000000000000000");
    private static final DecimalFormat formatting = new DecimalFormat("#0.##########");

    public static String weiToEth(final BigInteger wei) {
        final BigDecimal bd = new BigDecimal(wei).divide(weiToEthRatio);
        return formatting.format(bd);
    }
}
