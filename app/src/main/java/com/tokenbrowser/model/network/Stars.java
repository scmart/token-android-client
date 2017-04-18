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
