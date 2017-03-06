package com.tokenbrowser.model.network;

import com.squareup.moshi.Json;

public class Stars {
    @Json(name = "1")
    private int one;
    @Json(name = "2")
    private int two;
    @Json(name = "3")
    private int three;
    @Json(name = "4")
    private int four;
    @Json(name = "5")
    private int five;

    public int getAmountOfOneStarRatings(final int level) {
        switch (level) {
            case 1: return one;
            case 2: return two;
            case 3: return three;
            case 4: return four;
            case 5: return five;
            default: return 0;
        }
    }
}
