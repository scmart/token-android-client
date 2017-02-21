package com.bakkenbaeck.token.crypto.signal.network;


import com.bakkenbaeck.token.model.network.ServerTime;

import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.PUT;
import retrofit2.http.Query;
import rx.Single;

public interface SignalInterface {

    @GET("/v1/accounts/bootstrap")
    Single<ServerTime> getTimestamp();

    @Headers("Content-Type: application/json")
    @PUT("/v1/accounts/bootstrap")
    Single<Void> register(@Body String body,
                          @Query("timestamp") long timestamp);
}
