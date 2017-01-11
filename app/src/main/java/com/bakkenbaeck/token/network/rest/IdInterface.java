package com.bakkenbaeck.token.network.rest;


import com.bakkenbaeck.token.model.User;
import com.bakkenbaeck.token.network.rest.model.ServerTime;
import com.bakkenbaeck.token.network.rest.model.SignedUserDetails;
import com.bakkenbaeck.token.network.rest.model.UserSearchResults;

import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;
import rx.Single;

public interface IdInterface {

    @GET("/v1/timestamp")
    Single<ServerTime> getTimestamp();

    @POST("/v1/user")
    Single<User> registerUser(@Body SignedUserDetails details);

    @GET("/v1/user/{id}")
    Single<User> getUser(@Path("id") String userId);

    @PUT("/v1/user/{id}")
    Single<User> updateUser(@Path("id") String userId, @Body SignedUserDetails details);

    @GET("/v1/search/user")
    Single<UserSearchResults> searchByUsername(@Query("query") String username);
}
