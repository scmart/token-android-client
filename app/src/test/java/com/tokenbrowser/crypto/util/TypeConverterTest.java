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
