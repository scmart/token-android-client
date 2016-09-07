package com.bakkenbaeck.toshi.network.rest;


import com.bakkenbaeck.toshi.model.CryptoDetails;
import com.bakkenbaeck.toshi.model.User;

import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import rx.Observable;

public interface ToshiInterface {

    @POST("/user")
    Observable<User> requestUserId();

    @GET("/user/{id}")
    Observable<User> getUser(@Path("id") String userId);

    @PUT("/user/{id}")
    Observable<Void> putUserCryptoDetails(@Header("TOSHIAPP-AUTH-TOKEN") String userAuthToken,
                                          @Path("id") String userId,
                                          @Body CryptoDetails details);
}
