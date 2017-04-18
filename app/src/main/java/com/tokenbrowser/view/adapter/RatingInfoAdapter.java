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

package com.tokenbrowser.view.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tokenbrowser.model.network.ReputationScore;
import com.tokenbrowser.R;
import com.tokenbrowser.view.adapter.viewholder.RatingInfoViewHolder;

public class RatingInfoAdapter extends RecyclerView.Adapter<RatingInfoViewHolder> {

    private static final int MAX_ITEMS = 5;

    private ReputationScore reputationScore;

    public void setReputationScore(final ReputationScore reputationScore) {
        this.reputationScore = reputationScore;
        this.notifyDataSetChanged();
    }

    @Override
    public RatingInfoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item__rating_info, parent, false);
        return new RatingInfoViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(RatingInfoViewHolder holder, int position) {
        final int stars = MAX_ITEMS - position;
        holder.setStars(String.valueOf(stars));

        if (this.reputationScore == null) {
            holder.setPercentageRating(0);
            return;
        }

        final double rating = this.reputationScore.getStars().getAmountOfOneStarRatings(stars);
        final int ratingPercentage = (int)((rating / this.reputationScore.getCount()) * 100);
        holder.setPercentageRating(ratingPercentage);
    }

    @Override
    public int getItemCount() {
        return MAX_ITEMS;
    }
}
