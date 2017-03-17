package com.tokenbrowser.crypto.signal.network;


import com.tokenbrowser.model.network.ServerTime;

import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.PUT;
import retrofit2.http.Query;
import rx.Single;

public interface ChatInterface {

    @Headers("Cache-control: no-store")
    @GET("/v1/accounts/bootstrap")
    Single<ServerTime> getTimestamp();

    @Headers({"Content-Type: application/json", "Cache-control: no-store"})
    @PUT("/v1/accounts/bootstrap")
    Single<Void> register(@Body String body,
                          @Query("timestamp") long timestamp);
}
