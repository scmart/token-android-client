package com.bakkenbaeck.token.model.network;


import com.bakkenbaeck.token.crypto.util.TypeConverter;
import com.squareup.moshi.Json;

import java.math.BigInteger;

public class Balance {

    @Json(name = "confirmed_balance")
    private String confirmedBalanceAsHex;
    @Json(name = "unconfirmed_balance")
    private String unconfirmedBalanceAsHex;

    public BigInteger getConfirmedBalance() {
        return TypeConverter.StringHexToBigInteger(confirmedBalanceAsHex);
    }

    public BigInteger getUnconfirmedBalance() {
        return TypeConverter.StringHexToBigInteger(unconfirmedBalanceAsHex);
    }
}
