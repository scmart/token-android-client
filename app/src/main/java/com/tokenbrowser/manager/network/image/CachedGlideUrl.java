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

package com.tokenbrowser.manager.network.image;

import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.Headers;
import com.bumptech.glide.load.model.LazyHeaders;

public class CachedGlideUrl extends GlideUrl {

    private static final Headers CACHE_HEADERS_OFFLINE = new LazyHeaders.Builder()
            .addHeader(
                    "Cache-Control",
                    String.format("max-age=%s, max-stale=%s",
                            1000 * 60 * 60 * 24 * 14,
                            1000 * 60 * 60 * 24 * 14)
            ).build();

    public CachedGlideUrl(String url) {
        super(url, CACHE_HEADERS_OFFLINE);
    }


}