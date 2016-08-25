package com.bakkenbaeck.toshi.http;


import com.bakkenbaeck.toshi.model.User;

import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import rx.Observable;

public interface ToshiInterface {

    @POST("/user")
    Observable<User> requestUserId();

    @GET("/user/{id}")
    Observable<User> getUser(@Path("id") String userId);
}
