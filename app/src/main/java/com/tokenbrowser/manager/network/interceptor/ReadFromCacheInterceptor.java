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

package com.tokenbrowser.manager.network.interceptor;


import com.tokenbrowser.view.BaseApplication;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.CacheControl;
import okhttp3.Interceptor;
import okhttp3.Response;

import static com.google.common.net.HttpHeaders.CACHE_CONTROL;

public class ReadFromCacheInterceptor implements Interceptor {

    @Override
    public Response intercept(final Chain chain) throws IOException, IllegalStateException {
        final Response response = chain.proceed( chain.request() );

        CacheControl cacheControl = new CacheControl.Builder()
                .maxAge(60, TimeUnit.SECONDS)
                .build();

        if (!BaseApplication.get().isConnected()) {
            cacheControl = new CacheControl.Builder()
                    .maxAge(14, TimeUnit.DAYS)
                    .build();
        }

        return response.newBuilder()
                .header(CACHE_CONTROL, cacheControl.toString())
                .build();
    }
}
