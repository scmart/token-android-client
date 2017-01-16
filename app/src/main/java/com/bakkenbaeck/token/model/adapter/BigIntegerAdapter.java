package com.bakkenbaeck.token.model.adapter;


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
