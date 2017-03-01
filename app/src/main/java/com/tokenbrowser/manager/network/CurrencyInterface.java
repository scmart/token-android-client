package com.tokenbrowser.manager.network;

import com.tokenbrowser.model.network.MarketRates;

import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Single;

public interface CurrencyInterface {

    @GET("/v2/exchange-rates")
    Single<MarketRates> getRates(@Query("currency") String currency);
}
