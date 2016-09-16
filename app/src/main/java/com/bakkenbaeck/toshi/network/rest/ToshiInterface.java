package com.bakkenbaeck.toshi.network.rest;


import com.bakkenbaeck.toshi.model.CryptoDetails;
import com.bakkenbaeck.toshi.model.User;
import com.bakkenbaeck.toshi.network.rest.model.SignedWithdrawalRequest;
import com.bakkenbaeck.toshi.network.rest.model.WithdrawalRequest;
import com.bakkenbaeck.toshi.network.rest.model.SignatureRequest;

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

    @PUT("/user/{id}")
    Observable<Void> putUserCryptoDetails(@Header("TOSHIAPP-AUTH-TOKEN") String userAuthToken,
                                          @Path("id") String userId,
                                          @Body CryptoDetails details);

    @POST("/message")
    Observable<SignatureRequest> postWithdrawalRequest(@Header("TOSHIAPP-AUTH-TOKEN") String userAuthToken,
                                                       @Body WithdrawalRequest withdrawalRequest);

    @POST("/message")
    Observable<SignatureRequest> postSignedWithdrawal(@Header("TOSHIAPP-AUTH-TOKEN") String userAuthToken,
                                                      @Body SignedWithdrawalRequest signedWithdrawalRequest);
}
