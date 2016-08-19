package com.bakkenbaeck.toshi.http;


import com.bakkenbaeck.toshi.model.User;

import retrofit2.http.POST;
import rx.Observable;

public interface ToshiInterface {

    @POST("/user")
    Observable<User> requestUserId();
}
