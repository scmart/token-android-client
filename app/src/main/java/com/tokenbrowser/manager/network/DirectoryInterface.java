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

import com.tokenbrowser.model.network.App;
import com.tokenbrowser.model.network.Apps;

import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;
import rx.Observable;
import rx.Single;

public interface DirectoryInterface {

    @GET("/v1/apps/")
    Observable<Response<Apps>> getApps();

    @GET("/v1/apps/featured")
    Observable<Response<Apps>> getFeaturedApps();

    @GET("/v1/search/apps/")
    Observable<Response<Apps>> searchApps(@Query("query") String searchString);

    @GET("/v1/apps/{token_id}")
    Single<Response<App>> getApp(@Path("token_id") String tokenId);
}
