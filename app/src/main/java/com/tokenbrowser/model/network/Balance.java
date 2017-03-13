package com.tokenbrowser.model.network;


import com.squareup.moshi.Json;
import com.tokenbrowser.crypto.util.TypeConverter;
import com.tokenbrowser.token.R;
import com.tokenbrowser.util.EthUtil;
import com.tokenbrowser.view.BaseApplication;

import java.math.BigDecimal;
import java.math.BigInteger;

import rx.Single;
import rx.schedulers.Schedulers;

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


    public Single<String> getFormattedLocalBalance() {
        if (this.localBalance != null) {
            return Single.just(this.localBalance);
        }

        return BaseApplication
                .get()
                .getTokenManager()
                .getBalanceManager()
                .convertEthToLocalCurrencyString(EthUtil.weiToEth(getUnconfirmedBalance()))
                .subscribeOn(Schedulers.io());
    }

    public String getFormattedUnconfirmedBalance() {
        final BigDecimal unconfirmedEthBalance = EthUtil.weiToEth(getUnconfirmedBalance());
        return BaseApplication.get().getString(R.string.eth_balance, EthUtil.ethAmountToUserVisibleString(unconfirmedEthBalance));
    }
}
