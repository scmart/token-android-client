package com.bakkenbaeck.token.util;


import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.text.DecimalFormat;

public class EthUtil {

    private static final BigDecimal weiToEthRatio = new BigDecimal("1000000000000000000");
    private static final DecimalFormat formatting = new DecimalFormat("#0.##########");

    public static String weiToEthString(final BigInteger wei) {
        final BigDecimal bd = weiToEth(wei);
        return ethToEthString(bd);
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

    public static String ethToEthString(final BigDecimal eth) {
        formatting.setRoundingMode(RoundingMode.FLOOR);
        return formatting.format(eth);
    }
}
