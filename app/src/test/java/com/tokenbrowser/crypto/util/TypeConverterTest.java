package com.tokenbrowser.crypto.util;

import org.junit.Test;

import java.math.BigInteger;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class TypeConverterTest {

    @Test
    public void stringHexToBigIntegerReturnsZeroIfCalledWithNull() {
        final BigInteger expected = BigInteger.ZERO;
        final BigInteger actual = TypeConverter.StringHexToBigInteger(null);
        assertThat(actual, is(expected));
    }

    @Test
    public void stringHexToBigIntegerConvertsCorrectly() {
        // 1000000000 == 0x3B9ACA00
        final BigInteger expected = BigInteger.valueOf(1000000000L);
        final BigInteger actual = TypeConverter.StringHexToBigInteger("0x3B9ACA00");
        assertThat(actual, is(expected));
    }

    @Test
    public void stringHexToBigIntegerConvertsCorrectlyWithMissingHexPrefix() {
        // 1000000000 == 3B9ACA00
        final BigInteger expected = BigInteger.valueOf(1000000000L);
        final BigInteger actual = TypeConverter.StringHexToBigInteger("3B9ACA00");
        assertThat(actual, is(expected));
    }

    @Test
    public void stringHexToBigIntegerReturnZeroIfCalledWithInvalidHex() {
        final BigInteger expected = BigInteger.ZERO;
        final BigInteger actual = TypeConverter.StringHexToBigInteger("notHex");
        assertThat(actual, is(expected));
    }
}
