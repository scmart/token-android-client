package com.bakkenbaeck.token.network.rest;


import com.bakkenbaeck.token.model.User;
import com.bakkenbaeck.token.network.rest.model.ServerTime;
import com.bakkenbaeck.token.network.rest.model.SignatureRequest;
import com.bakkenbaeck.token.network.rest.model.SignedUserDetails;
import com.bakkenbaeck.token.network.rest.model.SignedWithdrawalRequest;
import com.bakkenbaeck.token.network.rest.model.TransactionSent;
import com.bakkenbaeck.token.network.rest.model.WithdrawalRequest;

import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import rx.Observable;
import rx.Single;

public interface TokenInterface {

    @GET("/v1/timestamp")
    Single<ServerTime> getTimestamp();

    @POST("/v1/user")
    Single<User> registerUser(@Body SignedUserDetails details);


    @GET("/user/{id}")
    Observable<User> getUser(@Path("id") String userId);



    @POST("/message")
    Observable<Response<SignatureRequest>> postWithdrawalRequest(@Body WithdrawalRequest withdrawalRequest);

    @POST("/message")
    Observable<Response<TransactionSent>> postSignedWithdrawal(@Body SignedWithdrawalRequest signedWithdrawalRequest);
}
