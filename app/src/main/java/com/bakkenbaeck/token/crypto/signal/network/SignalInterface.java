package com.bakkenbaeck.token.crypto.signal.network;


import com.bakkenbaeck.token.network.rest.model.ServerTime;

import retrofit2.http.GET;
import rx.Single;

public interface SignalInterface {

    @GET("/v1/accounts/bootstrap")
    Single<ServerTime> getTimestamp();
}
