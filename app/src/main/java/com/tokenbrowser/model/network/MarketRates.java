package com.tokenbrowser.model.network;


import java.math.BigDecimal;
import java.util.Map;

public class MarketRates {

    private static final long CACHE_TIMEOUT = 1000 * 60;

    private final long cacheTimestamp;

    // ctors
    public MarketRates() {
        this.cacheTimestamp = System.currentTimeMillis();
    }

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

    private DataPoints data;

    private static class DataPoints{
        private Map<String, BigDecimal> rates;
    }

    public boolean needsRefresh() {
        return System.currentTimeMillis() - cacheTimestamp > CACHE_TIMEOUT;
    }
}
