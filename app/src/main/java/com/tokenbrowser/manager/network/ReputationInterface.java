package com.tokenbrowser.manager.network;

import com.tokenbrowser.model.local.Review;
import com.tokenbrowser.model.network.ReputationScore;

import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;
import rx.Single;

public interface ReputationInterface {

    @GET("/v1/user/{owner_address}")
    Single<Response<ReputationScore>> getReputationScore(@Path("owner_address") String user);

    @POST("/v1/review/submit")
    Single<Response<Void>> submitReview(@Body Review review, @Query("timestamp") long timestamp);
}
