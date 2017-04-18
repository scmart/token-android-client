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

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.tokenbrowser.R;
import com.tokenbrowser.view.custom.RatingBar;

public class RatingInfoViewHolder extends RecyclerView.ViewHolder {

    private TextView stars;
    private RatingBar ratingBar;

    public RatingInfoViewHolder(View itemView) {
        super(itemView);

        this.stars = (TextView) itemView.findViewById(R.id.stars);
        this.ratingBar = (RatingBar) itemView.findViewById(R.id.rating_bar);
    }

    public void setStars(final String stars) {
        this.stars.setText(stars);
    }

    public void setPercentageRating(final int percentage) {
        this.ratingBar.setPercentage(percentage);
    }
}
