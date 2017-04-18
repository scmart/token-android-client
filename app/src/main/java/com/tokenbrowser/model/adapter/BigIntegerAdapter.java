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

package com.tokenbrowser.model.adapter;


import com.squareup.moshi.FromJson;
import com.squareup.moshi.ToJson;

import java.math.BigInteger;

public class BigIntegerAdapter {
    @ToJson
    String toJson(final BigInteger bigInteger) {
        return bigInteger.toString();
    }

    @FromJson
    BigInteger fromJson(final String bigInteger) {
        return new BigInteger(bigInteger);
    }
}
