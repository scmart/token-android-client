package com.tokenbrowser.model.network;


import com.tokenbrowser.token.R;
import com.tokenbrowser.crypto.util.TypeConverter;
import com.tokenbrowser.util.EthUtil;
import com.tokenbrowser.view.BaseApplication;
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
