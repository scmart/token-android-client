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
