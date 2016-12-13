package com.bakkenbaeck.token.network.rest;


import com.bakkenbaeck.token.model.User;
import com.bakkenbaeck.token.network.rest.model.ServerTime;
import com.bakkenbaeck.token.network.rest.model.SignatureRequest;
import com.bakkenbaeck.token.network.rest.model.SignedUserDetails;
import com.bakkenbaeck.token.network.rest.model.SignedTransactionRequest;
import com.bakkenbaeck.token.network.rest.model.TransactionSent;
import com.bakkenbaeck.token.network.rest.model.TransactionRequest;

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


    @GET("/v1/user/{id}")
    Single<User> getUser(@Path("id") String userId);

    @POST("/message")
    Observable<Response<SignatureRequest>> postTransactionRequest(@Body TransactionRequest transactionRequest);

    @POST("/message")
    Observable<Response<TransactionSent>> postSignedTransaction(@Body SignedTransactionRequest signedTransactionRequest);
}
