package com.tokenbrowser.manager.network;

import com.tokenbrowser.model.network.ReputationScore;

import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.Path;
import rx.Single;

public interface ReputationInterface {

    @GET("v1/user/{owner_address}")
    Single<Response<ReputationScore>> getReputationScore(@Path("owner_address") String user);
}
