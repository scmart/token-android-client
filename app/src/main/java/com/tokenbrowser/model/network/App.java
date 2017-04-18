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

public class App {
    private int review_count;
    private Double reputation_score;
    private String token_id;
    private Custom custom;
    private String payment_address;
    private boolean is_app;

    public int getReviewCount() {
        return review_count;
    }

    public Double getReputationScore() {
        return reputation_score;
    }


    public String getTokenId() {
        return token_id;
    }

    public Custom getCustom() {
        return custom;
    }

    public String getPaymentAddress() {
        return payment_address;
    }

    public boolean isApp() {
        return is_app;
    }
}
