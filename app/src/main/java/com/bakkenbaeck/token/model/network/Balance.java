package com.bakkenbaeck.token.model.network;


import com.bakkenbaeck.token.R;
import com.bakkenbaeck.token.crypto.util.TypeConverter;
import com.bakkenbaeck.token.util.EthUtil;
import com.bakkenbaeck.token.view.BaseApplication;
import com.squareup.moshi.Json;

import java.math.BigDecimal;
import java.math.BigInteger;

public class Balance {

    @Json(name = "confirmed_balance")
    private String confirmedBalanceAsHex;
    @Json(name = "unconfirmed_balance")
    private String unconfirmedBalanceAsHex;

    private String localBalance;

    public BigInteger getConfirmedBalance() {
        return TypeConverter.StringHexToBigInteger(confirmedBalanceAsHex);
    }

    public BigInteger getUnconfirmedBalance() {
        return TypeConverter.StringHexToBigInteger(unconfirmedBalanceAsHex);
    }


    public String getFormattedLocalBalance() {
        return localBalance;
    }

    public void setFormattedLocalBalance(final String localBalance) {
        this.localBalance = localBalance;
    }

    public String getFormattedUnconfirmedBalance() {
        final BigDecimal unconfirmedEthBalance = EthUtil.weiToEth(getUnconfirmedBalance());
        return BaseApplication.get().getString(R.string.eth_balance, EthUtil.ethToEthString(unconfirmedEthBalance));
    }
}
