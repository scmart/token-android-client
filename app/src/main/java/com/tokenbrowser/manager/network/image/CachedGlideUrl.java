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