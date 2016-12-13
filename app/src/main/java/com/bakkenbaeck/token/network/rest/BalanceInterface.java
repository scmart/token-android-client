package com.bakkenbaeck.token.network.rest;


import com.bakkenbaeck.token.network.rest.model.Balance;
import com.bakkenbaeck.token.network.rest.model.SentTransaction;
import com.bakkenbaeck.token.network.rest.model.SignedTransaction;
import com.bakkenbaeck.token.network.rest.model.TransactionRequest;
import com.bakkenbaeck.token.network.rest.model.UnsignedTransaction;

import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import rx.Single;

public interface BalanceInterface {

    @POST("/v1/tx/skel")
    Single<UnsignedTransaction> createTransaction(@Body TransactionRequest request);

    @POST("/v1/tx")
    Single<SentTransaction> sendSignedTransaction(@Body SignedTransaction transaction);


    @GET("/v1/balance/{id}")
    Single<Balance> getBalance(@Path("id") String walletAddress);
}
