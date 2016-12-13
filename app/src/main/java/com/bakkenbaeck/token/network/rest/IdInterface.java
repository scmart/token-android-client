package com.bakkenbaeck.token.network.rest;


import com.bakkenbaeck.token.model.User;
import com.bakkenbaeck.token.network.rest.model.ServerTime;
import com.bakkenbaeck.token.network.rest.model.SignedUserDetails;

import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import rx.Single;

public interface IdInterface {

    @GET("/v1/timestamp")
    Single<ServerTime> getTimestamp();

    @POST("/v1/user")
    Single<User> registerUser(@Body SignedUserDetails details);


    @GET("/v1/user/{id}")
    Single<User> getUser(@Path("id") String userId);
}
