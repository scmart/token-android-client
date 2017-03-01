package com.tokenbrowser.manager.network;


import com.tokenbrowser.model.local.User;
import com.tokenbrowser.model.network.ServerTime;
import com.tokenbrowser.model.network.UserDetails;
import com.tokenbrowser.model.network.UserSearchResults;

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

    @GET("/v1/login/{id}")
    Single<Void> webLogin(@Path("id") String loginToken,
                          @Query("timestamp") long timestamp);
}
