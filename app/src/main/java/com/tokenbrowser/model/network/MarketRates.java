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

package com.tokenbrowser.model.network;


import java.math.BigDecimal;
import java.util.Map;

public class MarketRates {

    private DataPoints data;

    // ctors
    public MarketRates() {}

    // Returns market rate for a currency or ZERO if
    // there is no data for that currency.
    public BigDecimal getRate(final String currency) {
        if (data == null || data.rates == null) {
            return BigDecimal.ZERO;
        }

        final BigDecimal rate = data.rates.get(currency);
        if (rate == null) {
            return BigDecimal.ZERO;
        }

        return rate;
    }

    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private static class DataPoints{
        private Map<String, BigDecimal> rates;
    }
}
