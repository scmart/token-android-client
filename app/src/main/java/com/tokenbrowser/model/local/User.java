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

package com.tokenbrowser.model.local;


import com.squareup.moshi.Json;
import com.tokenbrowser.manager.TokenManager;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class User extends RealmObject {

    @PrimaryKey
    @Json(name = "token_id")
    private String owner_address;
    private String payment_address;
    private String username;
    private CustomAppInformation customAppInfo;
    private long cacheTimestamp;
    private Double reputation_score;
    private int review_count;
    private String about;
    private String avatar;
    private String location;
    private String name;

    // ctors
    public User() {
        this.cacheTimestamp = System.currentTimeMillis();
    }

    public void setCustomAppInfo(final CustomAppInformation customAppInfo) {
        this.customAppInfo = customAppInfo;
    }

    // Getters

    public String getUsername() {
        return String.format("@%s", username);
    }

    public String getUsernameForEditing() {
        return this.username;
    }

    // Defaults to the username if no name is set.
    public String getDisplayName() {
        if (this.name == null) {
            return username;
        }
        return this.name;
    }

    public String getTokenId() {
        return owner_address;
    }

    public String getPaymentAddress() {
        return payment_address;
    }

    public String getAbout() {
        return this.about;
    }

    public String getAvatar() {
        return this.avatar;
    }

    public String getLocation() {
        return this.location;
    }

    // Setters

    public void setUsername(final String username) {
        this.username = username;
    }

    public boolean needsRefresh() {
        return System.currentTimeMillis() - cacheTimestamp > TokenManager.CACHE_TIMEOUT;
    }

    public Double getReputationScore() {
        return reputation_score;
    }

    public int getReviewCount() {
        return review_count;
    }

    @Override
    public boolean equals(Object other){
        if (other == null) return false;
        if (other == this) return true;
        if (!(other instanceof User)) return false;
        final User otherUser = (User) other;
        return otherUser.getTokenId().equals(this.getTokenId());
    }

    @Override
    public int hashCode() {
        return getTokenId().hashCode();
    }
}
