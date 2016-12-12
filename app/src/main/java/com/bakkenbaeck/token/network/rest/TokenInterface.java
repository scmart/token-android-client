package com.bakkenbaeck.token.network.rest;


import com.bakkenbaeck.token.model.User;
import com.bakkenbaeck.token.network.rest.model.ServerTime;
import com.bakkenbaeck.token.network.rest.model.SignatureRequest;
import com.bakkenbaeck.token.network.rest.model.SignedUserDetails;
import com.bakkenbaeck.token.network.rest.model.SignedWithdrawalRequest;
import com.bakkenbaeck.token.network.rest.model.TransactionSent;
import com.bakkenbaeck.token.network.rest.model.WebSocketConnectionDetails;
import com.bakkenbaeck.token.network.rest.model.WithdrawalRequest;

import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;
import rx.Observable;
import rx.Single;

public interface TokenInterface {

    @GET("/v1/timestamp")
    Single<ServerTime> getTimestamp();

    @GET("/user/{id}")
    Observable<User> getUser(@Header("TOSHIAPP-AUTH-TOKEN") String userAuthToken,
                             @Path("id") String userId);

    @GET("/api/v1/rtm.start")
    Observable<WebSocketConnectionDetails> getWebsocketUrl(@Header("TOSHIAPP-AUTH-TOKEN") String userAuthToken);

    @POST("/v1/user")
    Observable<User> registerUser(@Body SignedUserDetails details);

    @POST("/message")
    Observable<Response<SignatureRequest>> postWithdrawalRequest(@Header("TOSHIAPP-AUTH-TOKEN") String userAuthToken,
                                                                @Body WithdrawalRequest withdrawalRequest);

    @POST("/message")
    Observable<Response<TransactionSent>> postSignedWithdrawal(@Header("TOSHIAPP-AUTH-TOKEN") String userAuthToken,
                                                     @Body SignedWithdrawalRequest signedWithdrawalRequest);
}
