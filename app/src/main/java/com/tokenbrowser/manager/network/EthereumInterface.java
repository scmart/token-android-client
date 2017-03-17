package com.tokenbrowser.manager.network;

import com.tokenbrowser.model.network.Addresses;
import com.tokenbrowser.model.network.Balance;
import com.tokenbrowser.model.network.GcmRegistration;
import com.tokenbrowser.model.network.SentTransaction;
import com.tokenbrowser.model.network.ServerTime;
import com.tokenbrowser.model.network.SignedTransaction;
import com.tokenbrowser.model.network.TransactionRequest;
import com.tokenbrowser.model.network.UnsignedTransaction;

import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;
import rx.Single;

public interface EthereumInterface {

    @Headers("Cache-control: no-store")
    @POST("/v1/tx/skel")
    Single<UnsignedTransaction> createTransaction(@Body TransactionRequest request);

    @Headers("Cache-control: no-store")
    @POST("/v1/tx")
    Single<SentTransaction> sendSignedTransaction(
            @Query("timestamp") long timestamp,
            @Body SignedTransaction transaction);


    @GET("/v1/balance/{id}")
    Single<Balance> getBalance(@Path("id") String walletAddress);

    @Headers("Cache-control: no-store")
    @GET("/v1/timestamp")
    Single<ServerTime> getTimestamp();

    @Headers("Cache-control: no-store")
    @POST("/v1/gcm/register")
    Single<Void> registerGcm(
            @Query("timestamp") long timestamp,
            @Body GcmRegistration gcmRegistration);

    @Headers("Cache-control: no-store")
    @POST("/v1/register")
    Single<Void> startWatchingAddresses(
            @Query("timestamp") long timestamp,
            @Body Addresses addresses);

}
