package com.bakkenbaeck.token.model.adapter;


import com.squareup.moshi.FromJson;
import com.squareup.moshi.ToJson;

import java.math.BigDecimal;

public class BigDecimalAdapter {
    @ToJson
    String toJson(final BigDecimal bigDecimal) {
        return bigDecimal.toString();
    }

    @FromJson
    BigDecimal fromJson(final String bigDecimal) {
        return new BigDecimal(bigDecimal);
    }
}
