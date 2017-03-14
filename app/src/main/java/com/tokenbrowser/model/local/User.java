package com.tokenbrowser.model.local;


import com.tokenbrowser.token.R;
import com.tokenbrowser.manager.TokenManager;
import com.tokenbrowser.view.BaseApplication;
import com.squareup.moshi.Json;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class User extends RealmObject {

    @PrimaryKey
    @Json(name = "token_id")
    private String owner_address;
    private String payment_address;
    private String username;
    @Json(name = "custom")
    private CustomUserInformation customUserInfo;
    private CustomAppInformation customAppInfo;
    private long cacheTimestamp;
    private Double reputation_score;
    private int review_count;

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
        if (customUserInfo == null || customUserInfo.getName() == null) {
            return username;
        }
        return customUserInfo.getName();
    }

    public String getTokenId() {
        return owner_address;
    }

    public String getPaymentAddress() {
        return payment_address;
    }

    public String getAbout() {
        return customUserInfo == null ? null : this.customUserInfo.getAbout();
    }

    public String getAvatar() {
        return customUserInfo == null
                ? null
                : String.format("%s%s", BaseApplication.get().getResources().getString(R.string.id_url), this.customUserInfo.getAvatar());
    }

    public String getLocation() {
        return customUserInfo == null ? null : this.customUserInfo.getLocation();
    }

    private CustomUserInformation getCustomUserInfo() {
        return this.customUserInfo;
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
