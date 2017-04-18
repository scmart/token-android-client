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

package com.tokenbrowser.view.adapter.viewholder;


import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.tokenbrowser.R;
import com.tokenbrowser.model.local.User;
import com.tokenbrowser.util.ImageUtil;
import com.tokenbrowser.view.BaseApplication;
import com.tokenbrowser.view.custom.StarRatingView;

public class ContactViewHolder extends ClickableViewHolder {

    private ImageView avatar;
    private TextView name;
    private TextView username;
    private StarRatingView ratingView;
    private TextView reviewCount;

    public ContactViewHolder(final View view) {
        super(view);
        this.name = (TextView) view.findViewById(R.id.name);
        this.username = (TextView) view.findViewById(R.id.username);
        this.avatar = (ImageView) view.findViewById(R.id.avatar);
        this.ratingView = (StarRatingView) view.findViewById(R.id.rating_view);
        this.reviewCount = (TextView) view.findViewById(R.id.review_count);
    }

    public void setUser(final User user) {
        this.name.setText(user.getDisplayName());
        this.username.setText(user.getUsername());
        ImageUtil.loadFromNetwork(user.getAvatar(), this.avatar);

        final double reputationScore = user.getReputationScore() != null
                ? user.getReputationScore()
                : 0;

        this.ratingView.setStars(reputationScore);
        final String reviewCount = BaseApplication.get().getString(R.string.parentheses, user.getReviewCount());
        this.reviewCount.setText(reviewCount);
    }
}