package com.tokenbrowser.util;

import org.junit.Test;

import java.math.BigDecimal;
import java.math.BigInteger;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class EthUtilTest {

    @Test(expected = NullPointerException.class)
    public void weiToEthStringThrowsExceptionIfCalledWithNull() {
        EthUtil.weiToEthString(null);
    }

    @Test
    public void weiToEthStringCalledWithZeroWeiReturnsCorrectly() {
        final BigInteger zeroWei = BigInteger.ZERO;
        final String expected = "0";
        assertThat(EthUtil.weiToEthString(zeroWei), is(expected));
    }

    @Test
    public void weiToEthStringCalledWithLittleWeiTruncatesCorrectly() {
        // Output is limit to 10dp
        final BigInteger oneWei = BigInteger.ONE;
        final String expected = "0";
        assertThat(EthUtil.weiToEthString(oneWei), is(expected));
    }

    @Test
    public void weiToEthStringCalledWithOneEthReturnsCorrectly() {
        final BigInteger oneWei = new BigInteger("1000000000000000000");
        final String expected = "1";
        assertThat(EthUtil.weiToEthString(oneWei), is(expected));
    }

    @Test
    public void weiToEthStringRoundsDown() {
        final BigInteger oneWei = new BigInteger("1555555555555555555");
        final String expected = "1.5555555555";
        assertThat(EthUtil.weiToEthString(oneWei), is(expected));
    }

    @Test(expected = NullPointerException.class)
    public void ethToWeiThrowsExceptionIfCalledWithNull() {
        EthUtil.ethToWei(null);
    }

    @Test
    public void ethToWeiCalledWithZeroEthReturnsCorrectly() {
        final BigDecimal eth = BigDecimal.ZERO;
        final BigInteger expected = BigInteger.ZERO;
        assertThat(EthUtil.ethToWei(eth), is(expected));
    }

    @Test
    public void ethToWeiCalledWithOneEthReturnsCorrectly() {
        final BigDecimal eth = BigDecimal.ONE;
        final BigInteger expected = new BigInteger("1000000000000000000");
        assertThat(EthUtil.ethToWei(eth), is(expected));
    }

    @Test
    public void ethToWeiCalledWithDecimalPointReturnsCorrectly() {
        final BigDecimal eth = new BigDecimal(0.0000000001);
        final BigInteger expected = new BigInteger("100000000");
        assertThat(EthUtil.ethToWei(eth), is(expected));
    }
}