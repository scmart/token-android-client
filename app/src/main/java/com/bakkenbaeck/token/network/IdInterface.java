package com.bakkenbaeck.token.network;


import com.bakkenbaeck.token.model.local.User;
import com.bakkenbaeck.token.model.network.ServerTime;
import com.bakkenbaeck.token.model.network.UserDetails;
import com.bakkenbaeck.token.model.network.UserSearchResults;

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
    Single<User> registerUser(@Body UserDetails details,
                              @Query("timestamp") long timestamp);

    @GET("/v1/user/{id}")
    Single<User> getUser(@Path("id") String userId);

    @PUT("/v1/user/{id}")
    Single<User> updateUser(@Path("id") String userId,
                            @Body UserDetails details,
                            @Query("timestamp") long timestamp);

    @GET("/v1/search/user")
    Single<UserSearchResults> searchByUsername(@Query("query") String username);
}
