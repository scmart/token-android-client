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

import com.bumptech.glide.load.model.*;

public class ForceLoadGlideUrl extends GlideUrl {
    private static final Headers FORCE_ETAG_CHECK = new LazyHeaders.Builder()
            // Force server side validation for the ETAG
            .addHeader("Cache-Control", "max-age=0")
            .build();

    public ForceLoadGlideUrl(String url) {
        super(url, FORCE_ETAG_CHECK);
    }
}
