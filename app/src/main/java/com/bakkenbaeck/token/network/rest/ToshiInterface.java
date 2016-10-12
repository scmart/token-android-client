package com.bakkenbaeck.token.network.rest;


import com.bakkenbaeck.token.model.CryptoDetails;
import com.bakkenbaeck.token.model.User;
import com.bakkenbaeck.token.network.rest.model.SignatureRequest;
import com.bakkenbaeck.token.network.rest.model.SignedWithdrawalRequest;
import com.bakkenbaeck.token.network.rest.model.TransactionSent;
import com.bakkenbaeck.token.network.rest.model.WebSocketConnectionDetails;
import com.bakkenbaeck.token.network.rest.model.WithdrawalRequest;

import retrofit2.Response;
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
    Observable<User> getUser(@Header("TOSHIAPP-AUTH-TOKEN") String userAuthToken,
                             @Path("id") String userId);

    @GET("/api/v1/rtm.start")
    Observable<WebSocketConnectionDetails> getWebsocketUrl(@Header("TOSHIAPP-AUTH-TOKEN") String userAuthToken);

    @PUT("/user/{id}")
    Observable<Void> putUserCryptoDetails(@Header("TOSHIAPP-AUTH-TOKEN") String userAuthToken,
                                          @Path("id") String userId,
                                          @Body CryptoDetails details);

    @POST("/message")
    Observable<Response<SignatureRequest>> postWithdrawalRequest(@Header("TOSHIAPP-AUTH-TOKEN") String userAuthToken,
                                                                @Body WithdrawalRequest withdrawalRequest);

    @POST("/message")
    Observable<TransactionSent> postSignedWithdrawal(@Header("TOSHIAPP-AUTH-TOKEN") String userAuthToken,
                                                     @Body SignedWithdrawalRequest signedWithdrawalRequest);
}
