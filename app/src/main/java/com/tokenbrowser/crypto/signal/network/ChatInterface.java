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

package com.tokenbrowser.crypto.signal.network;


import com.tokenbrowser.model.network.ServerTime;

import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.PUT;
import retrofit2.http.Query;
import rx.Single;

public interface ChatInterface {

    @Headers("Cache-control: no-store")
    @GET("/v1/accounts/bootstrap")
    Single<ServerTime> getTimestamp();

    @Headers({"Content-Type: application/json", "Cache-control: no-store"})
    @PUT("/v1/accounts/bootstrap")
    Single<Void> register(@Body String body,
                          @Query("timestamp") long timestamp);
}
