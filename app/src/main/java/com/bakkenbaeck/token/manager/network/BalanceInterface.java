package com.bakkenbaeck.token.manager.network;

import com.bakkenbaeck.token.model.network.Addresses;
import com.bakkenbaeck.token.model.network.Balance;
import com.bakkenbaeck.token.model.network.GcmRegistration;
import com.bakkenbaeck.token.model.network.SentTransaction;
import com.bakkenbaeck.token.model.network.ServerTime;
import com.bakkenbaeck.token.model.network.SignedTransaction;
import com.bakkenbaeck.token.model.network.TransactionRequest;
import com.bakkenbaeck.token.model.network.UnsignedTransaction;

import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;
import rx.Single;

public interface BalanceInterface {

    @POST("/v1/tx/skel")
    Single<UnsignedTransaction> createTransaction(@Body TransactionRequest request);

    @POST("/v1/tx")
    Single<SentTransaction> sendSignedTransaction(
            @Query("timestamp") long timestamp,
            @Body SignedTransaction transaction);


    @GET("/v1/balance/{id}")
    Single<Balance> getBalance(@Path("id") String walletAddress);

    @GET("/v1/timestamp")
    Single<ServerTime> getTimestamp();

    @POST("/v1/gcm/register")
    Single<Void> registerGcm(
            @Query("timestamp") long timestamp,
            @Body GcmRegistration gcmRegistration);

    @POST("/v1/register")
    Single<Void> startWatchingAddresses(
            @Query("timestamp") long timestamp,
            @Body Addresses addresses);

}
