/*
 * 	Copyright (c) 2017. Token Browser, Inc
 *
 * 	This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.tokenbrowser.manager.network;

import com.tokenbrowser.model.network.Addresses;
import com.tokenbrowser.model.network.Balance;
import com.tokenbrowser.model.network.GcmRegistration;
import com.tokenbrowser.model.network.SentTransaction;
import com.tokenbrowser.model.network.ServerTime;
import com.tokenbrowser.model.network.SignedTransaction;
import com.tokenbrowser.model.network.TransactionRequest;
import com.tokenbrowser.model.network.UnsignedTransaction;

import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;
import rx.Single;

public interface EthereumInterface {

    @POST("/v1/tx/skel")
    Single<UnsignedTransaction> createTransaction(@Body TransactionRequest request);

    @POST("/v1/tx")
    Single<SentTransaction> sendSignedTransaction(
            @Query("timestamp") long timestamp,
            @Body SignedTransaction transaction);


    @GET("/v1/balance/{id}")
    Single<Balance> getBalance(@Path("id") String walletAddress);

    @GET("/v1/timestamp")
    Single<ServerTime> getTimestamp();

    @POST("/v1/gcm/register")
    Single<Void> registerGcm(
            @Query("timestamp") long timestamp,
            @Body GcmRegistration gcmRegistration);

    @POST("/v1/gcm/deregister")
    Single<Void> unregisterGcm(
            @Query("timestamp") long timestamp,
            @Body GcmRegistration gcmRegistration);

    @POST("/v1/register")
    Single<Void> startWatchingAddresses(
            @Query("timestamp") long timestamp,
            @Body Addresses addresses);
}
